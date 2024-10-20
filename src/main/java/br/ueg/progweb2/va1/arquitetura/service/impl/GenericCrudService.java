package br.ueg.progweb2.va1.arquitetura.service.impl;

import br.ueg.progweb2.va1.arquitetura.exceptions.ApiMessageCode;
import br.ueg.progweb2.va1.arquitetura.exceptions.BusinessException;
import br.ueg.progweb2.va1.arquitetura.mapper.GenericUpdateMapper;
import br.ueg.progweb2.va1.arquitetura.model.GenericModel;
import br.ueg.progweb2.va1.arquitetura.reflection.ReflectionUtils;
import br.ueg.progweb2.va1.arquitetura.service.CrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class GenericCrudService<
            MODEL extends GenericModel<TYPE_PK>,
            TYPE_PK,
            REPOSITORY extends JpaRepository<MODEL, TYPE_PK>
        > implements CrudService <
            MODEL,
            TYPE_PK
        >{
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private GenericUpdateMapper<MODEL, TYPE_PK> mapper;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    protected REPOSITORY repository;

    private Class<TYPE_PK> entityClass;

    public List<MODEL> listAll(){
        return repository.findAll();
    }

    @Override
    public MODEL create(MODEL dado) {
        prepareToCreate(dado);
        validateMandatoryFields(dado);
        validateBusinessLogic(dado);
        validateBusinessLogicForInsert(dado);
        MODEL saved = repository.saveAndFlush(dado);
        //TODO verificar para buscar os dados no banco novmente para atualizar dados relacionados
        return this.getById(saved.getId());
    }

    protected abstract void prepareToCreate(MODEL dado);

    @Override
    public MODEL update(MODEL dataToUpdate){
        var dataDB = validateIdModelExists(dataToUpdate.getId());
        validateMandatoryFields(dataToUpdate);
        validateBusinessLogic(dataToUpdate);
        validateBusinessLogicForUpdate(dataToUpdate);
        updateDataDBFromUpdate(dataToUpdate, dataDB);
        return repository.save(dataDB);
    }

    protected void updateDataDBFromUpdate(MODEL dataToUpdate, MODEL dataDB){
        mapper.updateModelFromModel(dataDB, dataToUpdate);
    };

    @Override
    public MODEL getById(TYPE_PK id){
        return this.validateIdModelExists(id);
    }

    @Override
    public MODEL deleteById(TYPE_PK id){
        MODEL modelToRemove = this.validateIdModelExists(id);
        this.repository.delete(modelToRemove);
        return modelToRemove;
    }

    private MODEL validateIdModelExists(TYPE_PK id){
        boolean valid = true;
        MODEL dadoBD = null;

        if(Objects.nonNull(id)) {
            dadoBD = this.internalGetById(id);
            if (dadoBD == null) {
                valid = false;
            }
        }else{
            valid = false;
        }

        if(Boolean.FALSE.equals(valid)){
            throw new BusinessException(ApiMessageCode.ERROR_RECORD_NOT_FOUND);
        }
        return dadoBD;
    }

    private MODEL internalGetById(TYPE_PK id){
        Optional<MODEL> byId = repository.findById(id);
        if(byId.isPresent()){
            return byId.get();
        }
        return null;
    }

    protected abstract void validateBusinessLogicForInsert(MODEL dado);

    protected abstract void validateBusinessLogicForUpdate(MODEL dado) ;

    protected abstract void validateBusinessLogic(MODEL dado) ;

    protected void validateMandatoryFields(MODEL dado) {
        List<String> mandatoryFieldsNotFilled = ReflectionUtils.getMandatoryFieldsNotFilled(dado);
        if (!mandatoryFieldsNotFilled.isEmpty()) {
            throw new BusinessException(ApiMessageCode.ERROR_MANDATORY_FIELDS, mandatoryFieldsNotFilled);
        }
    }

    public Class<TYPE_PK> getEntityType() {
        if(Objects.isNull(this.entityClass)){
            this.entityClass = (Class<TYPE_PK>) ((ParameterizedType) this.getClass()
                    .getGenericSuperclass()).getActualTypeArguments()[0];
        }
        return this.entityClass;
    }
}
