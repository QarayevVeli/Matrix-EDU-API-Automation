package az.matrix.api.data;

import java.util.LinkedHashMap;
import java.util.Map;

public final class TrainingBody {

    private TrainingBody() {
    }

    public static Map<String, Object> of(String name, String description) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", name);
        body.put("description", description);
        return body;
    }

    public static Map<String, Object> nameOnly(String name) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", name);
        return body;
    }

    public static Map<String, Object> update(long id, String name, String description) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", id);
        body.put("name", name);
        body.put("description", description);
        return body;
    }
}
