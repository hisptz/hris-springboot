package com.hisptz.hris.Bundles.RelationalFilter;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.hisptz.hris.Bundles.ResourceBundle.Resource;
import com.hisptz.hris.core.Model.ModelQuery;
import com.hisptz.hris.core.Model.ModelSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by Guest on 8/14/18.
 */
@Component
public class RelationalFilterQuery extends ModelQuery<RelationalFilter>  {
    @Autowired
    protected RelationalFilterRepository relationalFilterRepository;
    private ModelSpecification<RelationalFilter> spec;

    public RelationalFilterQuery(RelationalFilterRepository RelationalFilterRepository) {
        this.relationalFilterRepository = RelationalFilterRepository;
    }

    public RelationalFilter getRelationalFilterById(Long id){
      return relationalFilterRepository.findOne(id);
    }

    public List<RelationalFilter> RelationalFilters(String where, String sort){
        return query(where, spec, relationalFilterRepository, sort);
    }
}
