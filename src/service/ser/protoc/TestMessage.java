package service.ser.protoc;

import com.google.protobuf.InvalidProtocolBufferException;
import service.ser.BaseProtoMessage;

public  final class TestMessage implements BaseProtoMessage {
    //自定义encode()
    @Override
    public byte[] encode(Object message){
        return ((Message)message).toByteArray();
    }
    //自定义decode()
    @Override
    public Message decode(byte[] data) throws InvalidProtocolBufferException{return Message.parseFrom(data);}

    public TestMessage() {}
    public static void registerAllExtensions(
            com.google.protobuf.ExtensionRegistry registry) {
    }
    public interface MessageOrBuilder extends
            // @@protoc_insertion_point(interface_extends:Message)
            com.google.protobuf.MessageOrBuilder {

        /**
         * <code>optional string test = 1;</code>
         */
        String getTest();
        /**
         * <code>optional string test = 1;</code>
         */
        com.google.protobuf.ByteString
        getTestBytes();
    }
    /**
     * Protobuf type {@code Message}
     */
    public  static final class Message extends
            com.google.protobuf.GeneratedMessage implements
            // @@protoc_insertion_point(message_implements:Message)
            MessageOrBuilder {
        // Use Message.newBuilder() to construct.
        private Message(com.google.protobuf.GeneratedMessage.Builder builder) {
            super(builder);
        }
        private Message() {
            test_ = "";
        }

        @Override
        public final com.google.protobuf.UnknownFieldSet
        getUnknownFields() {
            return com.google.protobuf.UnknownFieldSet.getDefaultInstance();
        }
        private Message(
                com.google.protobuf.CodedInputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry) {
            this();
            int mutable_bitField0_ = 0;
            try {
                boolean done = false;
                while (!done) {
                    int tag = input.readTag();
                    switch (tag) {
                        case 0:
                            done = true;
                            break;
                        default: {
                            if (!input.skipField(tag)) {
                                done = true;
                            }
                            break;
                        }
                        case 10: {
                            com.google.protobuf.ByteString bs = input.readBytes();

                            test_ = bs;
                            break;
                        }
                    }
                }
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(e.setUnfinishedMessage(this));
            } catch (java.io.IOException e) {
                throw new RuntimeException(
                        new InvalidProtocolBufferException(
                                e.getMessage()).setUnfinishedMessage(this));
            } finally {
                makeExtensionsImmutable();
            }
        }
        public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
            return TestMessage.internal_static_Message_descriptor;
        }

        protected FieldAccessorTable
        internalGetFieldAccessorTable() {
            return TestMessage.internal_static_Message_fieldAccessorTable
                    .ensureFieldAccessorsInitialized(
                            Message.class, Builder.class);
        }

        public static final int TEST_FIELD_NUMBER = 1;
        private volatile Object test_;
        /**
         * <code>optional string test = 1;</code>
         */
        public String getTest() {
            Object ref = test_;
            if (ref instanceof String) {
                return (String) ref;
            } else {
                com.google.protobuf.ByteString bs =
                        (com.google.protobuf.ByteString) ref;
                String s = bs.toStringUtf8();
                if (bs.isValidUtf8()) {
                    test_ = s;
                }
                return s;
            }
        }
        /**
         * <code>optional string test = 1;</code>
         */
        public com.google.protobuf.ByteString
        getTestBytes() {
            Object ref = test_;
            if (ref instanceof String) {
                com.google.protobuf.ByteString b =
                        com.google.protobuf.ByteString.copyFromUtf8(
                                (String) ref);
                test_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        private byte memoizedIsInitialized = -1;
        public final boolean isInitialized() {
            byte isInitialized = memoizedIsInitialized;
            if (isInitialized == 1) return true;
            if (isInitialized == 0) return false;

            memoizedIsInitialized = 1;
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output)
                throws java.io.IOException {
            if (!getTestBytes().isEmpty()) {
                output.writeBytes(1, getTestBytes());
            }
        }

        private int memoizedSerializedSize = -1;
        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1) return size;

            size = 0;
            if (!getTestBytes().isEmpty()) {
                size += com.google.protobuf.CodedOutputStream
                        .computeBytesSize(1, getTestBytes());
            }
            memoizedSerializedSize = size;
            return size;
        }

        private static final long serialVersionUID = 0L;
        public static Message parseFrom(
                com.google.protobuf.ByteString data)
                throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }
        public static Message parseFrom(
                com.google.protobuf.ByteString data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }
        public static Message parseFrom(byte[] data)
                throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }
        public static Message parseFrom(
                byte[] data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }
        public static Message parseFrom(java.io.InputStream input)
                throws java.io.IOException {
            return PARSER.parseFrom(input);
        }
        public static Message parseFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }
        public static Message parseDelimitedFrom(java.io.InputStream input)
                throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input);
        }
        public static Message parseDelimitedFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input, extensionRegistry);
        }
        public static Message parseFrom(
                com.google.protobuf.CodedInputStream input)
                throws java.io.IOException {
            return PARSER.parseFrom(input);
        }
        public static Message parseFrom(
                com.google.protobuf.CodedInputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public Builder newBuilderForType() { return newBuilder(); }
        public static Builder newBuilder() {
            return DEFAULT_INSTANCE.toBuilder();
        }
        public static Builder newBuilder(Message prototype) {
            return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
        }
        public Builder toBuilder() {
            return this == DEFAULT_INSTANCE
                    ? new Builder() : new Builder().mergeFrom(this);
        }

        @Override
        protected Builder newBuilderForType(
                BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }
        /**
         * Protobuf type {@code Message}
         */
        public static final class Builder extends
                com.google.protobuf.GeneratedMessage.Builder<Builder> implements
                // @@protoc_insertion_point(builder_implements:Message)
                MessageOrBuilder {
            public static final com.google.protobuf.Descriptors.Descriptor
            getDescriptor() {
                return TestMessage.internal_static_Message_descriptor;
            }

            protected FieldAccessorTable
            internalGetFieldAccessorTable() {
                return TestMessage.internal_static_Message_fieldAccessorTable
                        .ensureFieldAccessorsInitialized(
                                Message.class, Builder.class);
            }

            // Construct using TestMessage.Message.newBuilder()
            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(
                    BuilderParent parent) {
                super(parent);
                maybeForceBuilderInitialization();
            }
            private void maybeForceBuilderInitialization() {
                if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
                }
            }
            public Builder clear() {
                super.clear();
                test_ = "";

                return this;
            }

            public com.google.protobuf.Descriptors.Descriptor
            getDescriptorForType() {
                return TestMessage.internal_static_Message_descriptor;
            }

            public Message getDefaultInstanceForType() {
                return Message.getDefaultInstance();
            }

            public Message build() {
                Message result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            public Message buildPartial() {
                Message result = new Message(this);
                result.test_ = test_;
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof Message) {
                    return mergeFrom((Message)other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(Message other) {
                if (other == Message.getDefaultInstance()) return this;
                if (!other.getTest().isEmpty()) {
                    test_ = other.test_;
                    onChanged();
                }
                onChanged();
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(
                    com.google.protobuf.CodedInputStream input,
                    com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                    throws java.io.IOException {
                Message parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (InvalidProtocolBufferException e) {
                    parsedMessage = (Message) e.getUnfinishedMessage();
                    throw e;
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }

            private Object test_ = "";
            /**
             * <code>optional string test = 1;</code>
             */
            public String getTest() {
                Object ref = test_;
                if (!(ref instanceof String)) {
                    com.google.protobuf.ByteString bs =
                            (com.google.protobuf.ByteString) ref;
                    String s = bs.toStringUtf8();
                    if (bs.isValidUtf8()) {
                        test_ = s;
                    }
                    return s;
                } else {
                    return (String) ref;
                }
            }
            /**
             * <code>optional string test = 1;</code>
             */
            public com.google.protobuf.ByteString
            getTestBytes() {
                Object ref = test_;
                if (ref instanceof String) {
                    com.google.protobuf.ByteString b =
                            com.google.protobuf.ByteString.copyFromUtf8(
                                    (String) ref);
                    test_ = b;
                    return b;
                } else {
                    return (com.google.protobuf.ByteString) ref;
                }
            }
            /**
             * <code>optional string test = 1;</code>
             */
            public Builder setTest(
                    String value) {
                if (value == null) {
                    throw new NullPointerException();
                }

                test_ = value;
                onChanged();
                return this;
            }
            /**
             * <code>optional string test = 1;</code>
             */
            public Builder clearTest() {

                test_ = getDefaultInstance().getTest();
                onChanged();
                return this;
            }
            /**
             * <code>optional string test = 1;</code>
             */
            public Builder setTestBytes(
                    com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }

                test_ = value;
                onChanged();
                return this;
            }
            public final Builder setUnknownFields(
                    final com.google.protobuf.UnknownFieldSet unknownFields) {
                return this;
            }

            public final Builder mergeUnknownFields(
                    final com.google.protobuf.UnknownFieldSet unknownFields) {
                return this;
            }


            // @@protoc_insertion_point(builder_scope:Message)
        }

        // @@protoc_insertion_point(class_scope:Message)
        private static final Message DEFAULT_INSTANCE;
        static {
            DEFAULT_INSTANCE = new Message();
        }

        public static Message getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        public static final com.google.protobuf.Parser<Message> PARSER =
                new com.google.protobuf.AbstractParser<Message>() {
                    public Message parsePartialFrom(
                            com.google.protobuf.CodedInputStream input,
                            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                            throws InvalidProtocolBufferException {
                        try {
                            return new Message(input, extensionRegistry);
                        } catch (RuntimeException e) {
                            if (e.getCause() instanceof
                                    InvalidProtocolBufferException) {
                                throw (InvalidProtocolBufferException)
                                        e.getCause();
                            }
                            throw e;
                        }
                    }
                };

        public static com.google.protobuf.Parser<Message> parser() {
            return PARSER;
        }

        @Override
        public com.google.protobuf.Parser<Message> getParserForType() {
            return PARSER;
        }

        public Message getDefaultInstanceForType() {
            return DEFAULT_INSTANCE;
        }

    }

    private static com.google.protobuf.Descriptors.Descriptor
            internal_static_Message_descriptor;
    private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
            internal_static_Message_fieldAccessorTable;

    public static com.google.protobuf.Descriptors.FileDescriptor
    getDescriptor() {
        return descriptor;
    }
    private static com.google.protobuf.Descriptors.FileDescriptor
            descriptor;
    static {
        String[] descriptorData = {
                "\n\ntest.proto\"\027\n\007Message\022\014\n\004test\030\001 \001(\tB\rB" +
                        "\013TestMessageb\006proto3"
        };
        com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
                new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
                    public com.google.protobuf.ExtensionRegistry assignDescriptors(
                            com.google.protobuf.Descriptors.FileDescriptor root) {
                        descriptor = root;
                        return null;
                    }
                };
        com.google.protobuf.Descriptors.FileDescriptor
                .internalBuildGeneratedFileFrom(descriptorData,
                        new com.google.protobuf.Descriptors.FileDescriptor[] {
                        }, assigner);
        internal_static_Message_descriptor =
                getDescriptor().getMessageTypes().get(0);
        internal_static_Message_fieldAccessorTable = new
                com.google.protobuf.GeneratedMessage.FieldAccessorTable(
                internal_static_Message_descriptor,
                new String[] { "Test", });
    }

    // @@protoc_insertion_point(outer_class_scope)
}