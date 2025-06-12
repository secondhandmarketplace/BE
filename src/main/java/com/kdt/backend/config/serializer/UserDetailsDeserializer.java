package com.kdt.backend.config.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import java.io.IOException;

public class UserDetailsDeserializer extends StdDeserializer<UserDetails> {

    public UserDetailsDeserializer() {
        super(UserDetails.class);
    }

    @Override
    public UserDetails deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        String username = node.get("username").asText();
        String password = node.get("password").asText();
        return User.withUsername(username)
                .password(password)
                .roles("USER") // 필요에 따라 역할 추가
                .build();
    }
}
