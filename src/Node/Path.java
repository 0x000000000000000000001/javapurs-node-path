    // Port of Node/Path.js with Node's POSIX semantics.
    private static String __pathNormalize(String path) {
        if (path.isEmpty()) return ".";
        boolean absolute = path.startsWith("/");
        boolean trailingSlash = path.length() > 1 && path.endsWith("/");
        java.util.Deque<String> parts = new java.util.ArrayDeque<>();
        for (String part : path.split("/+")) {
            if (part.isEmpty() || part.equals(".")) continue;
            if (part.equals("..")) {
                if (!parts.isEmpty() && !parts.peekLast().equals("..")) parts.pollLast();
                else if (!absolute) parts.addLast("..");
            } else {
                parts.addLast(part);
            }
        }
        StringBuilder builder = new StringBuilder();
        if (absolute) builder.append('/');
        builder.append(String.join("/", parts));
        String result = builder.toString();
        if (result.isEmpty()) return ".";
        if (trailingSlash && !result.endsWith("/")) result = result + "/";
        return result;
    }

    private static String __pathDirName(String path) {
        if (path.isEmpty()) return ".";
        int end = path.length();
        while (end > 1 && path.charAt(end - 1) == '/') end--;
        int slash = path.lastIndexOf('/', end - 1);
        if (slash < 0) return ".";
        if (slash == 0) return "/";
        return path.substring(0, slash);
    }

    private static String __pathBaseName(String path) {
        int end = path.length();
        while (end > 1 && path.charAt(end - 1) == '/') end--;
        int slash = path.lastIndexOf('/', end - 1);
        return path.substring(slash + 1, end);
    }

    private static String __pathExtName(String path) {
        String base = __pathBaseName(path);
        int dot = base.lastIndexOf('.');
        return dot <= 0 ? "" : base.substring(dot);
    }

    private static String __pathJoin(java.util.List<Object> segments) {
        StringBuilder builder = new StringBuilder();
        for (Object segment : segments) {
            String part = (String) segment;
            if (part.isEmpty()) continue;
            if (builder.length() > 0) builder.append('/');
            builder.append(part);
        }
        return __pathNormalize(builder.toString());
    }

    public static Object normalize = (java.util.function.Function<Object, Object>) (p) ->
        __pathNormalize((String) p);

    public static Object concat = (java.util.function.Function<Object, Object>) (segments) ->
        __pathJoin(java.util.Arrays.asList((Object[]) segments));

    public static Object resolve = (java.util.function.Function<Object, Object>) (from) ->
        (java.util.function.Function<Object, Object>) (to) ->
        (java.util.function.Supplier<Object>) () -> {
            java.util.List<Object> combined = new java.util.ArrayList<>(java.util.Arrays.asList((Object[]) from));
            combined.add(to);
            String joined = __pathJoin(combined);
            if (joined.startsWith("/")) return joined;
            return __pathNormalize((String) System.getProperty("user.dir") + "/" + joined);
        };

    public static Object relative = (java.util.function.Function<Object, Object>) (from) ->
        (java.util.function.Function<Object, Object>) (to) -> {
            String[] left = __pathNormalize((String) from).split("/+");
            String[] right = __pathNormalize((String) to).split("/+");
            int common = 0;
            while (common < left.length && common < right.length && left[common].equals(right[common])) common++;
            java.util.List<String> up = new java.util.ArrayList<>();
            for (int index = common; index < left.length; index++) {
                if (!left[index].isEmpty()) up.add("..");
            }
            for (int index = common; index < right.length; index++) {
                if (!right[index].isEmpty()) up.add(right[index]);
            }
            return String.join("/", up);
        };

    public static Object dirname = (java.util.function.Function<Object, Object>) (p) ->
        __pathNormalize(__pathDirName((String) p));

    public static Object basename = (java.util.function.Function<Object, Object>) (p) ->
        __pathBaseName((String) p);

    public static Object basenameWithoutExt = (java.util.function.Function<Object, Object>) (p) ->
        (java.util.function.Function<Object, Object>) (ext) -> {
            String base = __pathBaseName((String) p);
            String suffix = (String) ext;
            return !suffix.isEmpty() && base.endsWith(suffix) ? base.substring(0, base.length() - suffix.length()) : base;
        };

    public static Object extname = (java.util.function.Function<Object, Object>) (p) ->
        __pathExtName((String) p);

    public static Object sep = "/";

    public static Object delimiter = ":";

    public static Object parse = (java.util.function.Function<Object, Object>) (p) -> {
        String path = (String) p;
        String base = __pathBaseName(path);
        String ext = __pathExtName(path);
        java.util.Map<String, Object> parsed = new java.util.LinkedHashMap<>();
        parsed.put("root", path.startsWith("/") ? "/" : "");
        parsed.put("dir", path.startsWith("/") ? __pathDirName(path) : __pathDirName(path));
        parsed.put("base", base);
        parsed.put("ext", ext);
        parsed.put("name", ext.isEmpty() ? base : base.substring(0, base.length() - ext.length()));
        return parsed;
    };

    public static Object isAbsolute = (java.util.function.Function<Object, Object>) (p) ->
        ((String) p).startsWith("/");
