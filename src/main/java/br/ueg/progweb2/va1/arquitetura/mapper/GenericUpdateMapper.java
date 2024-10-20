package br.ueg.progweb2.va1.arquitetura.mapper;

import br.ueg.progweb2.va1.arquitetura.model.GenericModel;
import org.mapstruct.MappingTarget;

public interface GenericUpdateMapper<
        MODEL extends GenericModel<TYPE_PK>,
        TYPE_PK
        > {
    /**
     * Atualiza o objeto entity com os dados
     * do objeto updateEntity, pegando apenas o atributos
     * preenchidos.
     * @param entity
     * @param updateEntity
     */
    void updateModelFromModel(@MappingTarget MODEL entity, MODEL updateEntity);
}
