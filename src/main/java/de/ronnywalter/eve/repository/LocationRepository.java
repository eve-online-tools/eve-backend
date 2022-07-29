package de.ronnywalter.eve.repository;

import de.ronnywalter.eve.model.Location;
import de.ronnywalter.eve.model.LocationType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Set;


public interface LocationRepository extends CrudRepository<Location, Long> {

    @Query("select s.id from #{#entityName} s where s.locationType = ?1")
    Set<Long> getStructureIds(LocationType type);

    List<Location> findByLocationType(LocationType structure);

    List<Location> findByLocationTypeAndAccessForbidden(LocationType structure, boolean b);

    //List<Location> findByRegionIdAndLocationTypeAndAccessForbiddenAndHasMarket(Integer id, LocationType structure, boolean b, boolean b1);
}
