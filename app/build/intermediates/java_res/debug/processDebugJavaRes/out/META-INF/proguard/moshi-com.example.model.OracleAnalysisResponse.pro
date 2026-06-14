-keepnames class com.example.model.OracleAnalysisResponse
-if class com.example.model.OracleAnalysisResponse
-keep class com.example.model.OracleAnalysisResponseJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.example.model.OracleAnalysisResponse
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-keepclassmembers class com.example.model.OracleAnalysisResponse {
    public synthetic <init>(java.util.List,java.util.List,java.util.List,java.util.List,java.util.List,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
