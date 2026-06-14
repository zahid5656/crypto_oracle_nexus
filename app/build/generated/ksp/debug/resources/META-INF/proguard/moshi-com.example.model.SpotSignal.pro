-keepnames class com.example.model.SpotSignal
-if class com.example.model.SpotSignal
-keep class com.example.model.SpotSignalJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.example.model.SpotSignal
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-keepclassmembers class com.example.model.SpotSignal {
    public synthetic <init>(java.lang.String,java.lang.String,double,double,double,double,int,java.lang.Double,java.lang.Double,java.lang.Double,java.lang.Integer,int,java.lang.String,java.lang.String,int,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,double,boolean,int,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
