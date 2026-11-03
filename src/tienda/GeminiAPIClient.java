package tienda;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GeminiAPIClient {
    private static final int MAX_INTENTOS = 3;
    private static final long[] ESPERAS_REINTENTO_MS = {1000, 2000};
    private static final String API_KEY = System.getenv("GEMINI_API_KEY");
    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1/models/gemini-3.8-flash:generateContent";
    private static final Pattern TEXTO_RESPUESTA =
            Pattern.compile("\"text\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");

    private GeminiAPIClient() {
    }

    public static String responder(String prompt) throws IOException {
        return callGeminiAPI(prompt);
    }

    public static String callGeminiAPI(String prompt) throws IOException {
        if (API_KEY == null || API_KEY.isBlank()) {
            throw new IOException("Define la variable de entorno GEMINI_API_KEY con tu clave de Gemini.");
        }

        String urlAutenticada = API_URL + "?key="
                + java.net.URLEncoder.encode(API_KEY.trim(), StandardCharsets.UTF_8);
        String requestBody = "{\"contents\":[{\"parts\":[{\"text\":\""
                + escapeJson(prompt) + "\"}]}]}";

        for (int intento = 1; intento <= MAX_INTENTOS; intento++) {
            HttpURLConnection connection =
                    (HttpURLConnection) new URL(urlAutenticada).openConnection();
            boolean reintentar = false;
            try {
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(60000);
                connection.setDoOutput(true);

                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                connection.setFixedLengthStreamingMode(input.length);
                try (OutputStream output = connection.getOutputStream()) {
                    output.write(input);
                }

                int responseCode = connection.getResponseCode();
                InputStream responseStream = responseCode >= 200 && responseCode < 300
                        ? connection.getInputStream() : connection.getErrorStream();
                String response = responseStream == null ? "" : readResponse(responseStream);
                if (responseCode < 200 || responseCode >= 300) {
                    if (esErrorTemporal(responseCode) && intento < MAX_INTENTOS) {
                        reintentar = true;
                    } else if (response.toLowerCase(java.util.Locale.ROOT).contains("<html")) {
                        throw new IOException("El servidor devolvió HTTP " + responseCode
                                + " en formato HTML. Verifica la clave de Gemini y sus restricciones, "
                                + "o prueba sin proxy/VPN.");
                    } else {
                        throw new IOException("Error en la API de Gemini (HTTP "
                                + responseCode + "): " + response);
                    }
                } else {
                    return extractTextFromResponse(response);
                }
            } finally {
                connection.disconnect();
            }

            if (reintentar) {
                esperarAntesDeReintentar(intento);
            }
        }
        throw new IOException("No se pudo completar la consulta a Gemini.");
    }

    private static boolean esErrorTemporal(int responseCode) {
        return responseCode == 429 || (responseCode >= 500 && responseCode <= 599);
    }

    private static void esperarAntesDeReintentar(int intento) throws IOException {
        try {
            Thread.sleep(ESPERAS_REINTENTO_MS[intento - 1]);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("Se interrumpió la espera para reintentar la consulta a Gemini.", ex);
        }
    }

    private static String readResponse(InputStream input) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    private static String escapeJson(String input) {
        StringBuilder escaped = new StringBuilder(input.length() + 16);
        for (int i = 0; i < input.length(); i++) {
            char character = input.charAt(i);
            switch (character) {
                case '"': escaped.append("\\\""); break;
                case '\\': escaped.append("\\\\"); break;
                case '\b': escaped.append("\\b"); break;
                case '\f': escaped.append("\\f"); break;
                case '\n': escaped.append("\\n"); break;
                case '\r': escaped.append("\\r"); break;
                case '\t': escaped.append("\\t"); break;
                default:
                    if (character < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) character));
                    } else {
                        escaped.append(character);
                    }
            }
        }
        return escaped.toString();
    }

    private static String extractTextFromResponse(String jsonResponse) throws IOException {
        int candidatesStart = jsonResponse.indexOf("\"candidates\"");
        if (candidatesStart < 0) {
            throw new IOException("Gemini no devolvió una respuesta de texto.");
        }

        Matcher matches = TEXTO_RESPUESTA.matcher(jsonResponse.substring(candidatesStart));
        StringBuilder text = new StringBuilder();
        while (matches.find()) {
            text.append(unescapeJson(matches.group(1)));
        }
        if (text.length() == 0) {
            throw new IOException("Gemini no devolvió texto para esta consulta.");
        }
        return text.toString().trim();
    }

    private static String unescapeJson(String input) throws IOException {
        StringBuilder result = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char character = input.charAt(i);
            if (character != '\\') {
                result.append(character);
                continue;
            }
            if (++i >= input.length()) {
                throw new IOException("Gemini devolvió texto JSON incompleto.");
            }
            char escaped = input.charAt(i);
            switch (escaped) {
                case '"': result.append('"'); break;
                case '\\': result.append('\\'); break;
                case '/': result.append('/'); break;
                case 'b': result.append('\b'); break;
                case 'f': result.append('\f'); break;
                case 'n': result.append('\n'); break;
                case 'r': result.append('\r'); break;
                case 't': result.append('\t'); break;
                case 'u':
                    if (i + 4 >= input.length()) {
                        throw new IOException("Gemini devolvió un carácter JSON incompleto.");
                    }
                    try {
                        result.append((char) Integer.parseInt(input.substring(i + 1, i + 5), 16));
                    } catch (NumberFormatException ex) {
                        throw new IOException("Gemini devolvió un carácter JSON no válido.", ex);
                    }
                    i += 4;
                    break;
                default:
                    throw new IOException("Gemini devolvió una secuencia JSON no válida.");
            }
        }
        return result.toString();
    }
}
