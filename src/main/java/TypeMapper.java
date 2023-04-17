public class TypeMapper {

    static String map(Object o) {
        return switch (o.getClass().getSimpleName().toLowerCase()) {
            case "string" -> "text";
            case "int", "integer" -> "int";
            case "short" -> "smallint";
            case "long" -> "bigint";
            case "float" -> "real";
            case "double" -> "double precision";
            case "boolean" -> "boolean";
            default -> throw new RuntimeException("Unsupported type: " + o.getClass().getSimpleName());
        };
    }
}
