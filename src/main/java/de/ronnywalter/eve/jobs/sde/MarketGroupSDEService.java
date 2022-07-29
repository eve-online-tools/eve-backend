package de.ronnywalter.eve.jobs.sde;

import de.ronnywalter.eve.dto.MarketGroupDTO;
import de.ronnywalter.eve.dto.MyOrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@FeignClient (name = "evetoolsuite-sde/marketgroups")
public interface MarketGroupSDEService {

    @RequestMapping(method = RequestMethod.GET, value = "")
    List<MarketGroupDTO> getMarketGroups();
}
