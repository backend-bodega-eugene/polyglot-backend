package com.xray.app.proxyman.command;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 *
 */
@javax.annotation.Generated(
        value = "by gRPC proto compiler (version 1.54.1)",
        comments = "Source: xcore/command/command.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class HandlerServiceGrpc {

    private HandlerServiceGrpc() {
    }

    public static final String SERVICE_NAME = "xray.app.proxyman.command.HandlerService";

    // Static method descriptors that strictly reflect the proto.
    private static volatile io.grpc.MethodDescriptor<com.xray.app.proxyman.command.AddInboundRequest,
            com.xray.app.proxyman.command.AddInboundResponse> getAddInboundMethod;

    @io.grpc.stub.annotations.RpcMethod(
            fullMethodName = SERVICE_NAME + '/' + "AddInbound",
            requestType = com.xray.app.proxyman.command.AddInboundRequest.class,
            responseType = com.xray.app.proxyman.command.AddInboundResponse.class,
            methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
    public static io.grpc.MethodDescriptor<com.xray.app.proxyman.command.AddInboundRequest,
            com.xray.app.proxyman.command.AddInboundResponse> getAddInboundMethod() {
        io.grpc.MethodDescriptor<com.xray.app.proxyman.command.AddInboundRequest, com.xray.app.proxyman.command.AddInboundResponse> getAddInboundMethod;
        if ((getAddInboundMethod = HandlerServiceGrpc.getAddInboundMethod) == null) {
            synchronized (HandlerServiceGrpc.class) {
                if ((getAddInboundMethod = HandlerServiceGrpc.getAddInboundMethod) == null) {
                    HandlerServiceGrpc.getAddInboundMethod = getAddInboundMethod =
                            io.grpc.MethodDescriptor.<com.xray.app.proxyman.command.AddInboundRequest, com.xray.app.proxyman.command.AddInboundResponse>newBuilder()
                                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                                    .setFullMethodName(generateFullMethodName(SERVICE_NAME, "AddInbound"))
                                    .setSampledToLocalTracing(true)
                                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            com.xray.app.proxyman.command.AddInboundRequest.getDefaultInstance()))
                                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            com.xray.app.proxyman.command.AddInboundResponse.getDefaultInstance()))
                                    .setSchemaDescriptor(new HandlerServiceMethodDescriptorSupplier("AddInbound"))
                                    .build();
                }
            }
        }
        return getAddInboundMethod;
    }

    private static volatile io.grpc.MethodDescriptor<com.xray.app.proxyman.command.RemoveInboundRequest,
            com.xray.app.proxyman.command.RemoveInboundResponse> getRemoveInboundMethod;

    @io.grpc.stub.annotations.RpcMethod(
            fullMethodName = SERVICE_NAME + '/' + "RemoveInbound",
            requestType = com.xray.app.proxyman.command.RemoveInboundRequest.class,
            responseType = com.xray.app.proxyman.command.RemoveInboundResponse.class,
            methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
    public static io.grpc.MethodDescriptor<com.xray.app.proxyman.command.RemoveInboundRequest,
            com.xray.app.proxyman.command.RemoveInboundResponse> getRemoveInboundMethod() {
        io.grpc.MethodDescriptor<com.xray.app.proxyman.command.RemoveInboundRequest, com.xray.app.proxyman.command.RemoveInboundResponse> getRemoveInboundMethod;
        if ((getRemoveInboundMethod = HandlerServiceGrpc.getRemoveInboundMethod) == null) {
            synchronized (HandlerServiceGrpc.class) {
                if ((getRemoveInboundMethod = HandlerServiceGrpc.getRemoveInboundMethod) == null) {
                    HandlerServiceGrpc.getRemoveInboundMethod = getRemoveInboundMethod =
                            io.grpc.MethodDescriptor.<com.xray.app.proxyman.command.RemoveInboundRequest, com.xray.app.proxyman.command.RemoveInboundResponse>newBuilder()
                                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                                    .setFullMethodName(generateFullMethodName(SERVICE_NAME, "RemoveInbound"))
                                    .setSampledToLocalTracing(true)
                                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            com.xray.app.proxyman.command.RemoveInboundRequest.getDefaultInstance()))
                                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            com.xray.app.proxyman.command.RemoveInboundResponse.getDefaultInstance()))
                                    .setSchemaDescriptor(new HandlerServiceMethodDescriptorSupplier("RemoveInbound"))
                                    .build();
                }
            }
        }
        return getRemoveInboundMethod;
    }

    private static volatile io.grpc.MethodDescriptor<com.xray.app.proxyman.command.AlterInboundRequest,
            com.xray.app.proxyman.command.AlterInboundResponse> getAlterInboundMethod;

    @io.grpc.stub.annotations.RpcMethod(
            fullMethodName = SERVICE_NAME + '/' + "AlterInbound",
            requestType = com.xray.app.proxyman.command.AlterInboundRequest.class,
            responseType = com.xray.app.proxyman.command.AlterInboundResponse.class,
            methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
    public static io.grpc.MethodDescriptor<com.xray.app.proxyman.command.AlterInboundRequest,
            com.xray.app.proxyman.command.AlterInboundResponse> getAlterInboundMethod() {
        io.grpc.MethodDescriptor<com.xray.app.proxyman.command.AlterInboundRequest, com.xray.app.proxyman.command.AlterInboundResponse> getAlterInboundMethod;
        if ((getAlterInboundMethod = HandlerServiceGrpc.getAlterInboundMethod) == null) {
            synchronized (HandlerServiceGrpc.class) {
                if ((getAlterInboundMethod = HandlerServiceGrpc.getAlterInboundMethod) == null) {
                    HandlerServiceGrpc.getAlterInboundMethod = getAlterInboundMethod =
                            io.grpc.MethodDescriptor.<com.xray.app.proxyman.command.AlterInboundRequest, com.xray.app.proxyman.command.AlterInboundResponse>newBuilder()
                                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                                    .setFullMethodName(generateFullMethodName(SERVICE_NAME, "AlterInbound"))
                                    .setSampledToLocalTracing(true)
                                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            com.xray.app.proxyman.command.AlterInboundRequest.getDefaultInstance()))
                                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            com.xray.app.proxyman.command.AlterInboundResponse.getDefaultInstance()))
                                    .setSchemaDescriptor(new HandlerServiceMethodDescriptorSupplier("AlterInbound"))
                                    .build();
                }
            }
        }
        return getAlterInboundMethod;
    }

    private static volatile io.grpc.MethodDescriptor<com.xray.app.proxyman.command.AddOutboundRequest,
            com.xray.app.proxyman.command.AddOutboundResponse> getAddOutboundMethod;

    @io.grpc.stub.annotations.RpcMethod(
            fullMethodName = SERVICE_NAME + '/' + "AddOutbound",
            requestType = com.xray.app.proxyman.command.AddOutboundRequest.class,
            responseType = com.xray.app.proxyman.command.AddOutboundResponse.class,
            methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
    public static io.grpc.MethodDescriptor<com.xray.app.proxyman.command.AddOutboundRequest,
            com.xray.app.proxyman.command.AddOutboundResponse> getAddOutboundMethod() {
        io.grpc.MethodDescriptor<com.xray.app.proxyman.command.AddOutboundRequest, com.xray.app.proxyman.command.AddOutboundResponse> getAddOutboundMethod;
        if ((getAddOutboundMethod = HandlerServiceGrpc.getAddOutboundMethod) == null) {
            synchronized (HandlerServiceGrpc.class) {
                if ((getAddOutboundMethod = HandlerServiceGrpc.getAddOutboundMethod) == null) {
                    HandlerServiceGrpc.getAddOutboundMethod = getAddOutboundMethod =
                            io.grpc.MethodDescriptor.<com.xray.app.proxyman.command.AddOutboundRequest, com.xray.app.proxyman.command.AddOutboundResponse>newBuilder()
                                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                                    .setFullMethodName(generateFullMethodName(SERVICE_NAME, "AddOutbound"))
                                    .setSampledToLocalTracing(true)
                                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            com.xray.app.proxyman.command.AddOutboundRequest.getDefaultInstance()))
                                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            com.xray.app.proxyman.command.AddOutboundResponse.getDefaultInstance()))
                                    .setSchemaDescriptor(new HandlerServiceMethodDescriptorSupplier("AddOutbound"))
                                    .build();
                }
            }
        }
        return getAddOutboundMethod;
    }

    private static volatile io.grpc.MethodDescriptor<com.xray.app.proxyman.command.RemoveOutboundRequest,
            com.xray.app.proxyman.command.RemoveOutboundResponse> getRemoveOutboundMethod;

    @io.grpc.stub.annotations.RpcMethod(
            fullMethodName = SERVICE_NAME + '/' + "RemoveOutbound",
            requestType = com.xray.app.proxyman.command.RemoveOutboundRequest.class,
            responseType = com.xray.app.proxyman.command.RemoveOutboundResponse.class,
            methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
    public static io.grpc.MethodDescriptor<com.xray.app.proxyman.command.RemoveOutboundRequest,
            com.xray.app.proxyman.command.RemoveOutboundResponse> getRemoveOutboundMethod() {
        io.grpc.MethodDescriptor<com.xray.app.proxyman.command.RemoveOutboundRequest, com.xray.app.proxyman.command.RemoveOutboundResponse> getRemoveOutboundMethod;
        if ((getRemoveOutboundMethod = HandlerServiceGrpc.getRemoveOutboundMethod) == null) {
            synchronized (HandlerServiceGrpc.class) {
                if ((getRemoveOutboundMethod = HandlerServiceGrpc.getRemoveOutboundMethod) == null) {
                    HandlerServiceGrpc.getRemoveOutboundMethod = getRemoveOutboundMethod =
                            io.grpc.MethodDescriptor.<com.xray.app.proxyman.command.RemoveOutboundRequest, com.xray.app.proxyman.command.RemoveOutboundResponse>newBuilder()
                                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                                    .setFullMethodName(generateFullMethodName(SERVICE_NAME, "RemoveOutbound"))
                                    .setSampledToLocalTracing(true)
                                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            com.xray.app.proxyman.command.RemoveOutboundRequest.getDefaultInstance()))
                                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            com.xray.app.proxyman.command.RemoveOutboundResponse.getDefaultInstance()))
                                    .setSchemaDescriptor(new HandlerServiceMethodDescriptorSupplier("RemoveOutbound"))
                                    .build();
                }
            }
        }
        return getRemoveOutboundMethod;
    }

    private static volatile io.grpc.MethodDescriptor<com.xray.app.proxyman.command.AlterOutboundRequest,
            com.xray.app.proxyman.command.AlterOutboundResponse> getAlterOutboundMethod;

    @io.grpc.stub.annotations.RpcMethod(
            fullMethodName = SERVICE_NAME + '/' + "AlterOutbound",
            requestType = com.xray.app.proxyman.command.AlterOutboundRequest.class,
            responseType = com.xray.app.proxyman.command.AlterOutboundResponse.class,
            methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
    public static io.grpc.MethodDescriptor<com.xray.app.proxyman.command.AlterOutboundRequest,
            com.xray.app.proxyman.command.AlterOutboundResponse> getAlterOutboundMethod() {
        io.grpc.MethodDescriptor<com.xray.app.proxyman.command.AlterOutboundRequest, com.xray.app.proxyman.command.AlterOutboundResponse> getAlterOutboundMethod;
        if ((getAlterOutboundMethod = HandlerServiceGrpc.getAlterOutboundMethod) == null) {
            synchronized (HandlerServiceGrpc.class) {
                if ((getAlterOutboundMethod = HandlerServiceGrpc.getAlterOutboundMethod) == null) {
                    HandlerServiceGrpc.getAlterOutboundMethod = getAlterOutboundMethod =
                            io.grpc.MethodDescriptor.<com.xray.app.proxyman.command.AlterOutboundRequest, com.xray.app.proxyman.command.AlterOutboundResponse>newBuilder()
                                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                                    .setFullMethodName(generateFullMethodName(SERVICE_NAME, "AlterOutbound"))
                                    .setSampledToLocalTracing(true)
                                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            com.xray.app.proxyman.command.AlterOutboundRequest.getDefaultInstance()))
                                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            com.xray.app.proxyman.command.AlterOutboundResponse.getDefaultInstance()))
                                    .setSchemaDescriptor(new HandlerServiceMethodDescriptorSupplier("AlterOutbound"))
                                    .build();
                }
            }
        }
        return getAlterOutboundMethod;
    }

    /**
     * Creates a new async stub that supports all call types for the service
     */
    public static HandlerServiceStub newStub(io.grpc.Channel channel) {
        io.grpc.stub.AbstractStub.StubFactory<HandlerServiceStub> factory =
                new io.grpc.stub.AbstractStub.StubFactory<HandlerServiceStub>() {
                    @java.lang.Override
                    public HandlerServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
                        return new HandlerServiceStub(channel, callOptions);
                    }
                };
        return HandlerServiceStub.newStub(factory, channel);
    }

    /**
     * Creates a new blocking-style stub that supports unary and streaming output calls on the service
     */
    public static HandlerServiceBlockingStub newBlockingStub(
            io.grpc.Channel channel) {
        io.grpc.stub.AbstractStub.StubFactory<HandlerServiceBlockingStub> factory =
                new io.grpc.stub.AbstractStub.StubFactory<HandlerServiceBlockingStub>() {
                    @java.lang.Override
                    public HandlerServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
                        return new HandlerServiceBlockingStub(channel, callOptions);
                    }
                };
        return HandlerServiceBlockingStub.newStub(factory, channel);
    }

    /**
     * Creates a new ListenableFuture-style stub that supports unary calls on the service
     */
    public static HandlerServiceFutureStub newFutureStub(
            io.grpc.Channel channel) {
        io.grpc.stub.AbstractStub.StubFactory<HandlerServiceFutureStub> factory =
                new io.grpc.stub.AbstractStub.StubFactory<HandlerServiceFutureStub>() {
                    @java.lang.Override
                    public HandlerServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
                        return new HandlerServiceFutureStub(channel, callOptions);
                    }
                };
        return HandlerServiceFutureStub.newStub(factory, channel);
    }

    /**
     *
     */
    public interface AsyncService {

        /**
         *
         */
        default void addInbound(com.xray.app.proxyman.command.AddInboundRequest request,
                                io.grpc.stub.StreamObserver<com.xray.app.proxyman.command.AddInboundResponse> responseObserver) {
            io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getAddInboundMethod(), responseObserver);
        }

        /**
         *
         */
        default void removeInbound(com.xray.app.proxyman.command.RemoveInboundRequest request,
                                   io.grpc.stub.StreamObserver<com.xray.app.proxyman.command.RemoveInboundResponse> responseObserver) {
            io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRemoveInboundMethod(), responseObserver);
        }

        /**
         *
         */
        default void alterInbound(com.xray.app.proxyman.command.AlterInboundRequest request,
                                  io.grpc.stub.StreamObserver<com.xray.app.proxyman.command.AlterInboundResponse> responseObserver) {
            io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getAlterInboundMethod(), responseObserver);
        }

        /**
         *
         */
        default void addOutbound(com.xray.app.proxyman.command.AddOutboundRequest request,
                                 io.grpc.stub.StreamObserver<com.xray.app.proxyman.command.AddOutboundResponse> responseObserver) {
            io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getAddOutboundMethod(), responseObserver);
        }

        /**
         *
         */
        default void removeOutbound(com.xray.app.proxyman.command.RemoveOutboundRequest request,
                                    io.grpc.stub.StreamObserver<com.xray.app.proxyman.command.RemoveOutboundResponse> responseObserver) {
            io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRemoveOutboundMethod(), responseObserver);
        }

        /**
         *
         */
        default void alterOutbound(com.xray.app.proxyman.command.AlterOutboundRequest request,
                                   io.grpc.stub.StreamObserver<com.xray.app.proxyman.command.AlterOutboundResponse> responseObserver) {
            io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getAlterOutboundMethod(), responseObserver);
        }
    }

    /**
     * Base class for the server implementation of the service HandlerService.
     */
    public static abstract class HandlerServiceImplBase
            implements io.grpc.BindableService, AsyncService {

        @java.lang.Override
        public final io.grpc.ServerServiceDefinition bindService() {
            return HandlerServiceGrpc.bindService(this);
        }
    }

    /**
     * A stub to allow clients to do asynchronous rpc calls to service HandlerService.
     */
    public static final class HandlerServiceStub
            extends io.grpc.stub.AbstractAsyncStub<HandlerServiceStub> {
        private HandlerServiceStub(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @java.lang.Override
        protected HandlerServiceStub build(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new HandlerServiceStub(channel, callOptions);
        }

        /**
         *
         */
        public void addInbound(com.xray.app.proxyman.command.AddInboundRequest request,
                               io.grpc.stub.StreamObserver<com.xray.app.proxyman.command.AddInboundResponse> responseObserver) {
            io.grpc.stub.ClientCalls.asyncUnaryCall(
                    getChannel().newCall(getAddInboundMethod(), getCallOptions()), request, responseObserver);
        }

        /**
         *
         */
        public void removeInbound(com.xray.app.proxyman.command.RemoveInboundRequest request,
                                  io.grpc.stub.StreamObserver<com.xray.app.proxyman.command.RemoveInboundResponse> responseObserver) {
            io.grpc.stub.ClientCalls.asyncUnaryCall(
                    getChannel().newCall(getRemoveInboundMethod(), getCallOptions()), request, responseObserver);
        }

        /**
         *
         */
        public void alterInbound(com.xray.app.proxyman.command.AlterInboundRequest request,
                                 io.grpc.stub.StreamObserver<com.xray.app.proxyman.command.AlterInboundResponse> responseObserver) {
            io.grpc.stub.ClientCalls.asyncUnaryCall(
                    getChannel().newCall(getAlterInboundMethod(), getCallOptions()), request, responseObserver);
        }

        /**
         *
         */
        public void addOutbound(com.xray.app.proxyman.command.AddOutboundRequest request,
                                io.grpc.stub.StreamObserver<com.xray.app.proxyman.command.AddOutboundResponse> responseObserver) {
            io.grpc.stub.ClientCalls.asyncUnaryCall(
                    getChannel().newCall(getAddOutboundMethod(), getCallOptions()), request, responseObserver);
        }

        /**
         *
         */
        public void removeOutbound(com.xray.app.proxyman.command.RemoveOutboundRequest request,
                                   io.grpc.stub.StreamObserver<com.xray.app.proxyman.command.RemoveOutboundResponse> responseObserver) {
            io.grpc.stub.ClientCalls.asyncUnaryCall(
                    getChannel().newCall(getRemoveOutboundMethod(), getCallOptions()), request, responseObserver);
        }

        /**
         *
         */
        public void alterOutbound(com.xray.app.proxyman.command.AlterOutboundRequest request,
                                  io.grpc.stub.StreamObserver<com.xray.app.proxyman.command.AlterOutboundResponse> responseObserver) {
            io.grpc.stub.ClientCalls.asyncUnaryCall(
                    getChannel().newCall(getAlterOutboundMethod(), getCallOptions()), request, responseObserver);
        }
    }

    /**
     * A stub to allow clients to do synchronous rpc calls to service HandlerService.
     */
    public static final class HandlerServiceBlockingStub
            extends io.grpc.stub.AbstractBlockingStub<HandlerServiceBlockingStub> {
        private HandlerServiceBlockingStub(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @java.lang.Override
        protected HandlerServiceBlockingStub build(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new HandlerServiceBlockingStub(channel, callOptions);
        }

        /**
         *
         */
        public com.xray.app.proxyman.command.AddInboundResponse addInbound(com.xray.app.proxyman.command.AddInboundRequest request) {
            return io.grpc.stub.ClientCalls.blockingUnaryCall(
                    getChannel(), getAddInboundMethod(), getCallOptions(), request);
        }

        /**
         *
         */
        public com.xray.app.proxyman.command.RemoveInboundResponse removeInbound(com.xray.app.proxyman.command.RemoveInboundRequest request) {
            return io.grpc.stub.ClientCalls.blockingUnaryCall(
                    getChannel(), getRemoveInboundMethod(), getCallOptions(), request);
        }

        /**
         *
         */
        public com.xray.app.proxyman.command.AlterInboundResponse alterInbound(com.xray.app.proxyman.command.AlterInboundRequest request) {
            return io.grpc.stub.ClientCalls.blockingUnaryCall(
                    getChannel(), getAlterInboundMethod(), getCallOptions(), request);
        }

        /**
         *
         */
        public com.xray.app.proxyman.command.AddOutboundResponse addOutbound(com.xray.app.proxyman.command.AddOutboundRequest request) {
            return io.grpc.stub.ClientCalls.blockingUnaryCall(
                    getChannel(), getAddOutboundMethod(), getCallOptions(), request);
        }

        /**
         *
         */
        public com.xray.app.proxyman.command.RemoveOutboundResponse removeOutbound(com.xray.app.proxyman.command.RemoveOutboundRequest request) {
            return io.grpc.stub.ClientCalls.blockingUnaryCall(
                    getChannel(), getRemoveOutboundMethod(), getCallOptions(), request);
        }

        /**
         *
         */
        public com.xray.app.proxyman.command.AlterOutboundResponse alterOutbound(com.xray.app.proxyman.command.AlterOutboundRequest request) {
            return io.grpc.stub.ClientCalls.blockingUnaryCall(
                    getChannel(), getAlterOutboundMethod(), getCallOptions(), request);
        }
    }

    /**
     * A stub to allow clients to do ListenableFuture-style rpc calls to service HandlerService.
     */
    public static final class HandlerServiceFutureStub
            extends io.grpc.stub.AbstractFutureStub<HandlerServiceFutureStub> {
        private HandlerServiceFutureStub(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @java.lang.Override
        protected HandlerServiceFutureStub build(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new HandlerServiceFutureStub(channel, callOptions);
        }

        /**
         *
         */
        public com.google.common.util.concurrent.ListenableFuture<com.xray.app.proxyman.command.AddInboundResponse> addInbound(
                com.xray.app.proxyman.command.AddInboundRequest request) {
            return io.grpc.stub.ClientCalls.futureUnaryCall(
                    getChannel().newCall(getAddInboundMethod(), getCallOptions()), request);
        }

        /**
         *
         */
        public com.google.common.util.concurrent.ListenableFuture<com.xray.app.proxyman.command.RemoveInboundResponse> removeInbound(
                com.xray.app.proxyman.command.RemoveInboundRequest request) {
            return io.grpc.stub.ClientCalls.futureUnaryCall(
                    getChannel().newCall(getRemoveInboundMethod(), getCallOptions()), request);
        }

        /**
         *
         */
        public com.google.common.util.concurrent.ListenableFuture<com.xray.app.proxyman.command.AlterInboundResponse> alterInbound(
                com.xray.app.proxyman.command.AlterInboundRequest request) {
            return io.grpc.stub.ClientCalls.futureUnaryCall(
                    getChannel().newCall(getAlterInboundMethod(), getCallOptions()), request);
        }

        /**
         *
         */
        public com.google.common.util.concurrent.ListenableFuture<com.xray.app.proxyman.command.AddOutboundResponse> addOutbound(
                com.xray.app.proxyman.command.AddOutboundRequest request) {
            return io.grpc.stub.ClientCalls.futureUnaryCall(
                    getChannel().newCall(getAddOutboundMethod(), getCallOptions()), request);
        }

        /**
         *
         */
        public com.google.common.util.concurrent.ListenableFuture<com.xray.app.proxyman.command.RemoveOutboundResponse> removeOutbound(
                com.xray.app.proxyman.command.RemoveOutboundRequest request) {
            return io.grpc.stub.ClientCalls.futureUnaryCall(
                    getChannel().newCall(getRemoveOutboundMethod(), getCallOptions()), request);
        }

        /**
         *
         */
        public com.google.common.util.concurrent.ListenableFuture<com.xray.app.proxyman.command.AlterOutboundResponse> alterOutbound(
                com.xray.app.proxyman.command.AlterOutboundRequest request) {
            return io.grpc.stub.ClientCalls.futureUnaryCall(
                    getChannel().newCall(getAlterOutboundMethod(), getCallOptions()), request);
        }
    }

    private static final int METHODID_ADD_INBOUND = 0;
    private static final int METHODID_REMOVE_INBOUND = 1;
    private static final int METHODID_ALTER_INBOUND = 2;
    private static final int METHODID_ADD_OUTBOUND = 3;
    private static final int METHODID_REMOVE_OUTBOUND = 4;
    private static final int METHODID_ALTER_OUTBOUND = 5;

    private static final class MethodHandlers<Req, Resp> implements
            io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
        private final AsyncService serviceImpl;
        private final int methodId;

        MethodHandlers(AsyncService serviceImpl, int methodId) {
            this.serviceImpl = serviceImpl;
            this.methodId = methodId;
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("unchecked")
        public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
            switch (methodId) {
                case METHODID_ADD_INBOUND:
                    serviceImpl.addInbound((com.xray.app.proxyman.command.AddInboundRequest) request,
                            (io.grpc.stub.StreamObserver<com.xray.app.proxyman.command.AddInboundResponse>) responseObserver);
                    break;
                case METHODID_REMOVE_INBOUND:
                    serviceImpl.removeInbound((com.xray.app.proxyman.command.RemoveInboundRequest) request,
                            (io.grpc.stub.StreamObserver<com.xray.app.proxyman.command.RemoveInboundResponse>) responseObserver);
                    break;
                case METHODID_ALTER_INBOUND:
                    serviceImpl.alterInbound((com.xray.app.proxyman.command.AlterInboundRequest) request,
                            (io.grpc.stub.StreamObserver<com.xray.app.proxyman.command.AlterInboundResponse>) responseObserver);
                    break;
                case METHODID_ADD_OUTBOUND:
                    serviceImpl.addOutbound((com.xray.app.proxyman.command.AddOutboundRequest) request,
                            (io.grpc.stub.StreamObserver<com.xray.app.proxyman.command.AddOutboundResponse>) responseObserver);
                    break;
                case METHODID_REMOVE_OUTBOUND:
                    serviceImpl.removeOutbound((com.xray.app.proxyman.command.RemoveOutboundRequest) request,
                            (io.grpc.stub.StreamObserver<com.xray.app.proxyman.command.RemoveOutboundResponse>) responseObserver);
                    break;
                case METHODID_ALTER_OUTBOUND:
                    serviceImpl.alterOutbound((com.xray.app.proxyman.command.AlterOutboundRequest) request,
                            (io.grpc.stub.StreamObserver<com.xray.app.proxyman.command.AlterOutboundResponse>) responseObserver);
                    break;
                default:
                    throw new AssertionError();
            }
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("unchecked")
        public io.grpc.stub.StreamObserver<Req> invoke(
                io.grpc.stub.StreamObserver<Resp> responseObserver) {
            switch (methodId) {
                default:
                    throw new AssertionError();
            }
        }
    }

    public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
        return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
                .addMethod(
                        getAddInboundMethod(),
                        io.grpc.stub.ServerCalls.asyncUnaryCall(
                                new MethodHandlers<
                                        com.xray.app.proxyman.command.AddInboundRequest,
                                        com.xray.app.proxyman.command.AddInboundResponse>(
                                        service, METHODID_ADD_INBOUND)))
                .addMethod(
                        getRemoveInboundMethod(),
                        io.grpc.stub.ServerCalls.asyncUnaryCall(
                                new MethodHandlers<
                                        com.xray.app.proxyman.command.RemoveInboundRequest,
                                        com.xray.app.proxyman.command.RemoveInboundResponse>(
                                        service, METHODID_REMOVE_INBOUND)))
                .addMethod(
                        getAlterInboundMethod(),
                        io.grpc.stub.ServerCalls.asyncUnaryCall(
                                new MethodHandlers<
                                        com.xray.app.proxyman.command.AlterInboundRequest,
                                        com.xray.app.proxyman.command.AlterInboundResponse>(
                                        service, METHODID_ALTER_INBOUND)))
                .addMethod(
                        getAddOutboundMethod(),
                        io.grpc.stub.ServerCalls.asyncUnaryCall(
                                new MethodHandlers<
                                        com.xray.app.proxyman.command.AddOutboundRequest,
                                        com.xray.app.proxyman.command.AddOutboundResponse>(
                                        service, METHODID_ADD_OUTBOUND)))
                .addMethod(
                        getRemoveOutboundMethod(),
                        io.grpc.stub.ServerCalls.asyncUnaryCall(
                                new MethodHandlers<
                                        com.xray.app.proxyman.command.RemoveOutboundRequest,
                                        com.xray.app.proxyman.command.RemoveOutboundResponse>(
                                        service, METHODID_REMOVE_OUTBOUND)))
                .addMethod(
                        getAlterOutboundMethod(),
                        io.grpc.stub.ServerCalls.asyncUnaryCall(
                                new MethodHandlers<
                                        com.xray.app.proxyman.command.AlterOutboundRequest,
                                        com.xray.app.proxyman.command.AlterOutboundResponse>(
                                        service, METHODID_ALTER_OUTBOUND)))
                .build();
    }

    private static abstract class HandlerServiceBaseDescriptorSupplier
            implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
        HandlerServiceBaseDescriptorSupplier() {
        }

        @java.lang.Override
        public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
            return com.xray.app.proxyman.command.Command.getDescriptor();
        }

        @java.lang.Override
        public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
            return getFileDescriptor().findServiceByName("HandlerService");
        }
    }

    private static final class HandlerServiceFileDescriptorSupplier
            extends HandlerServiceBaseDescriptorSupplier {
        HandlerServiceFileDescriptorSupplier() {
        }
    }

    private static final class HandlerServiceMethodDescriptorSupplier
            extends HandlerServiceBaseDescriptorSupplier
            implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
        private final String methodName;

        HandlerServiceMethodDescriptorSupplier(String methodName) {
            this.methodName = methodName;
        }

        @java.lang.Override
        public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
            return getServiceDescriptor().findMethodByName(methodName);
        }
    }

    private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

    public static io.grpc.ServiceDescriptor getServiceDescriptor() {
        io.grpc.ServiceDescriptor result = serviceDescriptor;
        if (result == null) {
            synchronized (HandlerServiceGrpc.class) {
                result = serviceDescriptor;
                if (result == null) {
                    serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
                            .setSchemaDescriptor(new HandlerServiceFileDescriptorSupplier())
                            .addMethod(getAddInboundMethod())
                            .addMethod(getRemoveInboundMethod())
                            .addMethod(getAlterInboundMethod())
                            .addMethod(getAddOutboundMethod())
                            .addMethod(getRemoveOutboundMethod())
                            .addMethod(getAlterOutboundMethod())
                            .build();
                }
            }
        }
        return result;
    }
}
