package com.dnnthanh.wallet.be.platform.stereotype;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;

class PlatformStereotypeTest {
    @Test
    void useCaseIsRuntimeComponentTypeStereotype() {
        assertStereotype(UseCase.class);
    }

    @Test
    void persistenceIsRuntimeComponentTypeStereotype() {
        assertStereotype(Persistence.class);
    }

    @Test
    void adapterIsRuntimeComponentTypeStereotype() {
        assertStereotype(Adapter.class);
    }

    private void assertStereotype(Class<?> annotationType) {
        assertNotNull(annotationType.getAnnotation(Component.class));
        assertNotNull(annotationType.getAnnotation(Documented.class));
        Retention retention = annotationType.getAnnotation(Retention.class);
        assertNotNull(retention);
        assertTrue(retention.value() == RetentionPolicy.RUNTIME);
        Target target = annotationType.getAnnotation(Target.class);
        assertNotNull(target);
        assertTrue(Arrays.asList(target.value()).contains(ElementType.TYPE));
    }
}
