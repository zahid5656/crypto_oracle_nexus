-keepnames class com.example.model.DeepInsightItem
-if class com.example.model.DeepInsightItem
-keep class com.example.model.DeepInsightItemJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
