package com.ventanasyaluminios.service.impl;

import com.ventanasyaluminios.dto.request.FeaturedLineRequest;
import com.ventanasyaluminios.dto.response.FeaturedLineResponse;
import com.ventanasyaluminios.entity.FeaturedLine;
import com.ventanasyaluminios.exception.ResourceNotFoundException;
import com.ventanasyaluminios.mapper.SettingsMapper;
import com.ventanasyaluminios.repository.FeaturedLineRepository;
import com.ventanasyaluminios.service.FeaturedLineService;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementación de {@link FeaturedLineService}. */
@Service
public class FeaturedLineServiceImpl implements FeaturedLineService {

    private final FeaturedLineRepository repository;
    private final SettingsMapper mapper;

    public FeaturedLineServiceImpl(FeaturedLineRepository repository, SettingsMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeaturedLineResponse> list() {
        return repository.findAllByOrderByPositionAsc().stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional
    public FeaturedLineResponse create(FeaturedLineRequest request) {
        FeaturedLine line = new FeaturedLine();
        mapper.apply(request, line);
        line.setPosition(repository.findMaxPosition() + 1);
        return mapper.toResponse(repository.save(line));
    }

    @Override
    @Transactional
    public FeaturedLineResponse update(Long id, FeaturedLineRequest request) {
        FeaturedLine line = find(id);
        mapper.apply(request, line);
        return mapper.toResponse(line);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    @Override
    @Transactional
    public List<FeaturedLineResponse> reorder(List<Long> ids) {
        var byId = repository.findAll().stream().collect(Collectors.toMap(FeaturedLine::getId, Function.identity()));
        Reordering.requireSameIds(byId.keySet(), ids);
        for (int i = 0; i < ids.size(); i++) {
            byId.get(ids.get(i)).setPosition(i + 1);
        }
        repository.flush();
        return list();
    }

    private FeaturedLine find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("La línea destacada no existe."));
    }
}
