package trmio.inc.ams.data.service;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import trmio.inc.ams.data.entity.Attendance;

@Service
public class AttendanceService {

    private final AttendanceRepository repository;

    public AttendanceService(AttendanceRepository repository) {
        this.repository = repository;
    }

    public Optional<Attendance> get(Long id) {
        return repository.findById(id);
    }

    public Attendance update(Attendance entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<Attendance> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Attendance> list(Pageable pageable, Specification<Attendance> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
