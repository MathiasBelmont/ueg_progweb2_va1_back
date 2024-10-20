package br.ueg.progweb2.va1.arquitetura.mapper;

import br.ueg.progweb2.va1.arquitetura.model.GenericModel;

public interface SimpleGenericMapper<
        DTO,
        MODEL extends GenericModel<TYPE_PK>,
        TYPE_PK
        > extends GenericMapper<
        DTO,
        DTO,
        DTO,
        DTO,
        MODEL,
        TYPE_PK
        > {

}
