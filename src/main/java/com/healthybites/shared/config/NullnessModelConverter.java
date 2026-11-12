package com.healthybites.shared.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JavaType;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.models.SpecVersion;
import io.swagger.v3.oas.models.media.Schema;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.jspecify.annotations.Nullable;
import org.springframework.data.web.PagedModel;

/**
 * Lleva al contrato OpenAPI la nulabilidad del código. Todo el proyecto es {@code @NullMarked} (JSpecify), así que en
 * los {@code record} de la API un componente sin {@code @Nullable} nunca es null: se marca como obligatorio
 * ({@code required}). Los {@code @Nullable} siguen presentes en el JSON (Jackson escribe los null), pero admiten
 * {@code null}.
 *
 * <p>Así los tipos que genera el web-app distinguen {@code string} de {@code string | null} en lugar de marcarlo todo
 * como opcional. También se completan las páginas ({@code PagedModel}), que vienen de Spring Data.
 */
class NullnessModelConverter implements ModelConverter {

    private static final String OWN_PACKAGE = "com.healthybites";

    @Override
    @SuppressWarnings("rawtypes") // la firma de ModelConverter usa Schema sin parametrizar
    public @Nullable Schema resolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
        if (!chain.hasNext()) {
            return null;
        }
        Schema<?> resolved = chain.next().resolve(type, context, chain);
        Class<?> raw = rawClass(type.getType());
        if (resolved == null || raw == null) {
            return resolved;
        }
        Schema<?> model = definition(resolved, context);
        if (model == null || model.getProperties() == null) {
            return resolved;
        }
        if (raw.isRecord()
                && (raw.getPackageName().startsWith(OWN_PACKAGE) || raw.getEnclosingClass() == PagedModel.class)) {
            describeRecord(raw, model);
        } else if (PagedModel.class.isAssignableFrom(raw)) {
            require(model, "content");
            require(model, "page");
        }
        return resolved;
    }

    private static void describeRecord(Class<?> record, Schema<?> model) {
        @SuppressWarnings("rawtypes")
        Map<String, Schema> properties = model.getProperties();
        for (RecordComponent component : record.getRecordComponents()) {
            String name = jsonName(record, component);
            Schema<?> property = properties.get(name);
            if (property == null) {
                continue; // no se serializa (@JsonIgnore)
            }
            if (component.getAnnotatedType().isAnnotationPresent(Nullable.class)) {
                properties.put(name, nullable(property));
            } else {
                require(model, name);
            }
        }
    }

    private static void require(Schema<?> model, String property) {
        Set<String> required = new TreeSet<>(model.getRequired() == null ? List.of() : model.getRequired());
        if (required.add(property)) {
            model.setRequired(new ArrayList<>(required));
        }
    }

    /** La propiedad admite null: {@code type: [string, "null"]} en OpenAPI 3.1, o {@code oneOf} si es una referencia. */
    private static Schema<?> nullable(Schema<?> property) {
        if (property.get$ref() != null) {
            Schema<Object> reference = new Schema<>();
            reference.set$ref(property.get$ref());
            Schema<Object> wrapper = new Schema<>();
            wrapper.setOneOf(List.of(reference, nullType(property.getSpecVersion())));
            wrapper.setSpecVersion(property.getSpecVersion());
            return wrapper;
        }
        if (property.getSpecVersion() != SpecVersion.V31) {
            property.setNullable(true);
            return property;
        }
        Set<String> types = new LinkedHashSet<>(property.getTypes() == null ? Set.of() : property.getTypes());
        if (property.getType() != null) {
            types.add(property.getType());
        }
        if (types.isEmpty() || types.contains("null")) {
            return property;
        }
        types.add("null");
        property.setTypes(types);
        addNullToEnum(property);
        return property;
    }

    private static <T> void addNullToEnum(Schema<T> property) {
        List<T> values = property.getEnum();
        if (values != null && !values.contains(null)) {
            List<T> withNull = new ArrayList<>(values);
            withNull.add(null);
            property.setEnum(withNull);
        }
    }

    private static Schema<Object> nullType(SpecVersion version) {
        Schema<Object> schema = new Schema<>();
        schema.setSpecVersion(version);
        if (version == SpecVersion.V31) {
            schema.setTypes(Set.of("null"));
        } else {
            schema.setNullable(true);
        }
        return schema;
    }

    private static String jsonName(Class<?> record, RecordComponent component) {
        JsonProperty renamed = component.getAccessor().getAnnotation(JsonProperty.class);
        if (renamed == null) {
            try {
                renamed = record.getDeclaredField(component.getName()).getAnnotation(JsonProperty.class);
            } catch (NoSuchFieldException e) {
                renamed = null;
            }
        }
        return renamed != null && !renamed.value().isEmpty() ? renamed.value() : component.getName();
    }

    private static @Nullable Schema<?> definition(Schema<?> resolved, ModelConverterContext context) {
        String ref = resolved.get$ref();
        if (ref == null) {
            return resolved;
        }
        return context.getDefinedModels().get(ref.substring(ref.lastIndexOf('/') + 1));
    }

    private static @Nullable Class<?> rawClass(@Nullable Type type) {
        return switch (type) {
            case JavaType javaType -> javaType.getRawClass();
            case Class<?> clazz -> clazz;
            case ParameterizedType parameterized when parameterized.getRawType() instanceof Class<?> clazz -> clazz;
            case null, default -> null;
        };
    }
}
