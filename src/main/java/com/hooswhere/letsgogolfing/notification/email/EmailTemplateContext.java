package com.hooswhere.letsgogolfing.notification.email;

import java.util.HashMap;
import java.util.Map;

public class EmailTemplateContext {
    private final Map<String, Object> variables;

    private EmailTemplateContext(Map<String, Object> variables) {
        this.variables = new HashMap<>(variables);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Object get(String key) {
        return variables.get(key);
    }

    public String getString(String key) {
        Object value = variables.get(key);
        return value != null ? value.toString() : null;
    }

    public Map<String, Object> getVariables() {
        return new HashMap<>(variables);
    }

    public static class Builder {
        private final Map<String, Object> variables = new HashMap<>();

        public Builder put(String key, Object value) {
            variables.put(key, value);
            return this;
        }

        public Builder customer(String firstName, String lastName, String email, String companyName) {
            variables.put("firstName", firstName);
            variables.put("lastName", lastName);
            variables.put("email", email);
            variables.put("companyName", companyName);
            variables.put("fullName", (firstName + " " + lastName).trim());
            return this;
        }

        public Builder workflow(String workflowId, int currentStep) {
            variables.put("workflowId", workflowId);
            variables.put("currentStep", currentStep);
            return this;
        }

        public Builder putAll(Map<String, Object> additionalVariables) {
            if (additionalVariables != null) {
                variables.putAll(additionalVariables);
            }
            return this;
        }

        public EmailTemplateContext build() {
            return new EmailTemplateContext(variables);
        }
    }
}
