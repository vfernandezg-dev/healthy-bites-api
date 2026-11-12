package com.healthybites.notifications.app;

import com.healthybites.shared.config.AppProperties;
import com.healthybites.shared.security.Role;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Páginas del web-app a las que llevan los botones de los correos ({@code APP_FRONTEND_URL} + ruta). */
@Component
class AppLinks {

    private final AppProperties app;

    AppLinks(AppProperties app) {
        this.app = app;
    }

    /** El panel de inicio de cada rol. */
    String home(Role role) {
        return app.frontend(homePath(role));
    }

    String subscription() {
        return app.frontend("client/subscription");
    }

    String rewards() {
        return app.frontend("client/rewards");
    }

    /** Un grupo de la comunidad, dentro del espacio del rol de quien lo abre. */
    String group(Role role, UUID groupId) {
        return app.frontend(homePath(role) + "/community/groups/" + groupId);
    }

    private static String homePath(Role role) {
        return switch (role) {
            case CLIENT -> "client";
            case NUTRITIONIST -> "nutritionist";
            case ADMIN -> "admin";
        };
    }
}
