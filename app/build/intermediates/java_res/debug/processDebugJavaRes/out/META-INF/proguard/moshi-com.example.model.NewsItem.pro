-keepnames class com.example.model.NewsItem
-if class com.example.model.NewsItem
-keep class com.example.model.NewsItemJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.example.model.NewsItem
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-keepclassmembers class com.example.model.NewsItem {
    public synthetic <init>(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
