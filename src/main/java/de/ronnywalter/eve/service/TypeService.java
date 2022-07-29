package de.ronnywalter.eve.service;

import com.google.common.collect.Lists;
import de.ronnywalter.eve.model.MarketGroup;
import de.ronnywalter.eve.model.Type;
import de.ronnywalter.eve.model.TypeCategory;
import de.ronnywalter.eve.model.TypeGroup;
import de.ronnywalter.eve.repository.MarketGroupRepository;
import de.ronnywalter.eve.repository.TypeCategorityRepository;
import de.ronnywalter.eve.repository.TypeGroupRepository;
import de.ronnywalter.eve.repository.TypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TypeService {

    private final TypeCategorityRepository typeCategorityRepository;
    private final TypeGroupRepository typeGroupRepository;
    private final TypeRepository typeRepository;
    private final MarketGroupRepository marketGroupRepository;

    
    public TypeCategory saveTypeCategory(TypeCategory c) {
        return typeCategorityRepository.save(c);
    }

    public TypeCategory getTypeCategory(int id) {
        return typeCategorityRepository.findById(id).orElse(null);
    }
    
    public TypeGroup saveTypeGroup(TypeGroup g) {
        return typeGroupRepository.save(g);
    }

    public TypeGroup getTypeGroup(int id) {
        return typeGroupRepository.findById(id).orElse(null);
    }

    //@Cacheable("marketgroups")
    public MarketGroup getMarketGroup(int id) {
        return marketGroupRepository.findById(id).orElse(null);
    }

    public MarketGroup getMarketGroup(int id, boolean includeChilds) {
        MarketGroup mg = marketGroupRepository.findById(id).orElse(null);
        mg.setChildren(getChildrenForMarketGroup(mg));
        return mg;
    }

    public List<MarketGroup> getRootMarketGroups() {
        List<MarketGroup> groups = marketGroupRepository.findByParentIdOrderByName(null);
        return groups;
    }

    public List<MarketGroup> getRootMarketGroups(boolean includeChilds) {
        List<MarketGroup> groups = marketGroupRepository.findByParentIdOrderByName(null);
        if(includeChilds) {
            groups.forEach(marketGroup -> {
                marketGroup.setChildren(getChildrenForMarketGroup(marketGroup));
            });
        }
        return groups;
    }

    private List<MarketGroup> getChildrenForMarketGroup(MarketGroup mg) {
        if(mg != null) {
            List<MarketGroup> children = marketGroupRepository.findByParentIdOrderByName(mg.getId());
            if(children != null && children.size() > 0) {
                children.forEach(child -> {
                    child.setChildren(getChildrenForMarketGroup(child));
                });
                return children;
            }
        }
        return null;
    }


    //@CachePut(value = "marketgroups", key = "#g.id")
    public MarketGroup saveMarketGroup(MarketGroup g) {
        return marketGroupRepository.save(g);
    }

    public List<MarketGroup> saveMarketGroups(List<MarketGroup> groups) {
        return Lists.newArrayList(marketGroupRepository.saveAll(groups));
    }

    //@CachePut(value = "types", key = "#t.id")
    public Type saveType(Type t) {
        return typeRepository.save(t);
    }

    //@Cacheable("types")
    public Type getType(int id) {
        log.debug("Loading type " + id + " from db.");
        return typeRepository.findById(id).orElse(null);
    }

    public List<Type> getTypesOfMarketGroup(MarketGroup mg) {
        List<Type> types = new ArrayList<>();

        if(mg.isHasTypes()) {
            types.addAll(typeRepository.findByMarketGroupId(mg.getId()));
        }
        if(mg.getChildren() != null && mg.getChildren().size() > 0) {
            mg.getChildren().forEach(marketGroup -> {
                types.addAll(getTypesOfMarketGroup(marketGroup));
            });
        }
        return types;
    }

    public List<Type> getTypesOfMarketGroup(Integer marketGroupId) {
        MarketGroup mg = getMarketGroup(marketGroupId, true);
        return getTypesOfMarketGroup(mg);
    }

    public List<Integer> getPublishedTypeIds() {
        List<Type> types = typeRepository.findByPublished(true);
        List<Integer> typeIds = new ArrayList<>();
        types.forEach(t -> {
            typeIds.add(t.getId());
        });
        return typeIds;
    }

    public List<TypeCategory> saveTypeCategories(List<TypeCategory> typeCategories) {
        return Lists.newArrayList(typeCategorityRepository.saveAll(typeCategories));
    }

    public List<Type> getTypes() {
        return Lists.newArrayList(typeRepository.findAll());
    }
}
