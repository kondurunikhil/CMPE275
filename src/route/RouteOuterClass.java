// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: route.proto

package route;

public final class RouteOuterClass {
  private RouteOuterClass() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_route_Route_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_route_Route_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_route_HeartBeat_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_route_HeartBeat_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\013route.proto\022\005route\"h\n\005Route\022\n\n\002id\030\001 \001(" +
      "\003\022\016\n\006origin\030\002 \001(\003\022\023\n\013destination\030\003 \001(\003\022\014" +
      "\n\004path\030\004 \001(\t\022\017\n\007message\030\005 \001(\t\022\017\n\007payload" +
      "\030\006 \001(\014\"\031\n\tHeartBeat\022\014\n\004life\030\001 \001(\t2f\n\014Rou" +
      "teService\022%\n\007request\022\014.route.Route\032\014.rou" +
      "te.Route\022/\n\theartbeat\022\020.route.HeartBeat\032" +
      "\020.route.HeartBeatB\004H\001P\001b\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        });
    internal_static_route_Route_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_route_Route_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_route_Route_descriptor,
        new java.lang.String[] { "Id", "Origin", "Destination", "Path", "Message", "Payload", });
    internal_static_route_HeartBeat_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_route_HeartBeat_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_route_HeartBeat_descriptor,
        new java.lang.String[] { "Life", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
