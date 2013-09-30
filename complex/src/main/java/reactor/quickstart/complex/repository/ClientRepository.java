package reactor.quickstart.complex.repository;

import org.springframework.data.repository.CrudRepository;
import reactor.quickstart.complex.domain.Client;

/**
 * @author Jon Brisbin
 */
public interface ClientRepository extends CrudRepository<Client, Long> {
}
