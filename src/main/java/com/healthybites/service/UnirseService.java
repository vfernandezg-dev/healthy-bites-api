package com.healthybites.service;

import com.healthybites.dto.GrupoDTO;

public interface UnirseService {

    // Method to add a client to a group
    GrupoDTO addClientToGroup(Integer clientId, Integer groupId);

    // Method to remove a client from a group
    void removeClientFromGroup(Integer clientId, Integer groupId);
}
