package com.hotel.service;

import com.hotel.dto.RoomDTO;
import com.hotel.model.Room;
import com.hotel.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoomService {
    @Autowired
    private RoomRepository roomRepository;

    public Room createRoom(RoomDTO roomDTO) {
        if (roomRepository.existsByNumber(roomDTO.getNumber())) {
            throw new RuntimeException("Номер с таким названием уже существует");
        }

        Room room = new Room();
        room.setNumber(roomDTO.getNumber());
        room.setCapacity(roomDTO.getCapacity());

        return roomRepository.save(room);
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<RoomDTO> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public RoomDTO getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Номер не найден"));
        return convertToDTO(room);
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Room getRoomEntityById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Номер не найден"));
    }

    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }

    private RoomDTO convertToDTO(Room room) {
        RoomDTO dto = new RoomDTO();
        dto.setId(room.getId());
        dto.setNumber(room.getNumber());
        dto.setCapacity(room.getCapacity());
        return dto;
    }
}

