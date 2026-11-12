package com.healthybites.profiles.web;

import com.healthybites.profiles.app.ProfileService.ClientData;
import com.healthybites.profiles.app.ProfileService.Me;
import com.healthybites.profiles.domain.ClientProfile;
import com.healthybites.profiles.domain.NutritionistProfile;
import com.healthybites.profiles.web.ProfileDtos.ClientProfileResponse;
import com.healthybites.profiles.web.ProfileDtos.MeResponse;
import com.healthybites.profiles.web.ProfileDtos.NutritionistProfileResponse;
import java.time.LocalDate;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Entidades y vistas del módulo a los cuerpos de la API (MapStruct genera la implementación al compilar). */
@Mapper
interface ProfileMapper {

    @Mapping(target = "id", source = "account.id")
    @Mapping(target = "email", source = "account.email")
    @Mapping(target = "firstName", source = "account.firstName")
    @Mapping(target = "lastName", source = "account.lastName")
    @Mapping(target = "role", source = "account.role")
    @Mapping(target = "client", expression = "java(clientResponse(me.client(), me.today()))")
    @Mapping(target = "nutritionist", source = "nutritionist")
    MeResponse toResponse(Me me);

    NutritionistProfileResponse toResponse(NutritionistProfile profile);

    ClientData toData(ProfileDtos.ClientData data);

    /** La edad se calcula con la fecha de hoy (del reloj de la aplicación), no se guarda. */
    default @Nullable ClientProfileResponse clientResponse(@Nullable ClientProfile profile, LocalDate today) {
        if (profile == null) {
            return null;
        }
        return new ClientProfileResponse(
                profile.getSex(),
                profile.getBirthDate(),
                profile.ageOn(today),
                profile.getHeightCm(),
                profile.getWeightKg());
    }

    default ClientData toDataOrEmpty(ProfileDtos.@Nullable ClientData data) {
        return data == null ? new ClientData(null, null, null, null) : toData(data);
    }
}
