package com.technicalchallenge.mapper;

import com.technicalchallenge.dto.BookDTO;
import com.technicalchallenge.model.Book;
import com.technicalchallenge.model.CostCenter;
import com.technicalchallenge.repository.CostCenterRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

    private ModelMapper modelMapper;

    private CostCenterRepository costCenterRepository;

    public BookDTO toDto(Book entity) {
        BookDTO dto = modelMapper.map(entity, BookDTO.class);
        dto.setCostCenterName(entity.getCostCenter() != null ? entity.getCostCenter().getCostCenterName() : null);
        return dto;
    }

    public Book toEntity(BookDTO dto) {
        Book entity = modelMapper.map(dto, Book.class);
        if (dto.getCostCenterName() != null) {
            CostCenter costCenter = costCenterRepository.findAll().stream()
                .filter(cc -> dto.getCostCenterName().equals(cc.getCostCenterName()))
                .findFirst().orElse(null);
            entity.setCostCenter(costCenter);
        }
        return entity;
    }
}
