import org.jetbrains.annotations.Nullable;

public class NuLib {
    public String getJbValue(NullValueProvider provider) { return provider.provide(); }
    public static class NullValueProvider {
        @Nullable public String provide() { return "aaa"; }
    }
}