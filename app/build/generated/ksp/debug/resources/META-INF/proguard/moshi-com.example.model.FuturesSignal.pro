-keepnames class com.example.model.FuturesSignal
-if class com.example.model.FuturesSignal
-keep class com.example.model.FuturesSignalJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.example.model.FuturesSignal
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-keepclassmembers class com.example.model.FuturesSignal {
    public synthetic <init>(java.lang.String,java.lang.String,double,double,double,int,boolean,java.lang.Double,java.lang.Double,java.lang.Integer,int,java.lang.String,java.lang.String,int,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,double,boolean,int,int,int,int,boolean,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
