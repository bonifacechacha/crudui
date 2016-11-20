package org.vaadin.crudui.form;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.shared.util.SharedUtil;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Field;
import com.vaadin.ui.Notification;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.crudui.crud.CrudOperation;

import java.util.*;

/**
 * @author Alejandro Duarte.
 */
public abstract class AbstractAutoGeneratedCrudFormFactory<T> extends AbstractCrudFormFactory<T> {

    protected Map<CrudOperation, String> buttonCaptions = new HashMap<>();
    protected Map<CrudOperation, Resource> buttonIcons = new HashMap<>();
    protected Map<CrudOperation, Set<String>> buttonStyleNames = new HashMap<>();

    protected Class<T> domainType;
    protected String validationErrorMessage = "Please fix the errors and try again";

    public AbstractAutoGeneratedCrudFormFactory(Class<T> domainType) {
        this.domainType = domainType;

        setButtonCaption(CrudOperation.READ, "Ok");
        setButtonCaption(CrudOperation.ADD, "Add");
        setButtonCaption(CrudOperation.UPDATE, "Update");
        setButtonCaption(CrudOperation.DELETE, "Yes, delete");

        setButtonIcon(CrudOperation.READ, null);
        setButtonIcon(CrudOperation.ADD, FontAwesome.SAVE);
        setButtonIcon(CrudOperation.UPDATE, FontAwesome.SAVE);
        setButtonIcon(CrudOperation.DELETE, FontAwesome.TIMES);

        addButtonStyleName(CrudOperation.READ, null);
        addButtonStyleName(CrudOperation.ADD, ValoTheme.BUTTON_PRIMARY);
        addButtonStyleName(CrudOperation.UPDATE, ValoTheme.BUTTON_PRIMARY);
        addButtonStyleName(CrudOperation.DELETE, ValoTheme.BUTTON_DANGER);

        setVisiblePropertyIds(discoverPropertyIds().toArray());
    }

    public void setButtonCaption(CrudOperation operation, String caption) {
        buttonCaptions.put(operation, caption);
    }

    public void setButtonIcon(CrudOperation operation, Resource icon) {
        buttonIcons.put(operation, icon);
    }

    public void addButtonStyleName(CrudOperation operation, String styleName) {
        buttonStyleNames.putIfAbsent(operation, new HashSet<>());
        buttonStyleNames.get(operation).add(styleName);
    }

    public void setValidationErrorMessage(String validationErrorMessage) {
        this.validationErrorMessage = validationErrorMessage;
    }

    protected List<Object> discoverPropertyIds() {

        BeanItemContainer<T> propertyIdsHelper = new BeanItemContainer<T>(domainType);
        return new ArrayList<>(propertyIdsHelper.getContainerPropertyIds());
    }


    protected List<Field> buildAndBind(CrudOperation operation, T domainObject, boolean readOnly, BeanFieldGroup<T> fieldGroup) {
        ArrayList<Field> fields = new ArrayList<>();
        fieldGroup.setItemDataSource(domainObject);
        CrudFormConfiguration configuration = getConfiguration(operation);

        for (int i = 0; i < configuration.getVisiblePropertyIds().size(); i++) {
            Field<?> field = null;
            Object propertyId = configuration.getVisiblePropertyIds().get(i);

            FieldProvider provider = configuration.getFieldProviders().get(propertyId);
            if (provider != null) {
                field = provider.buildField();
            } else {
                Class<? extends Field> fieldType = configuration.getFieldTypes().get(propertyId);
                if (fieldType == null) {
                    fieldType = Field.class;
                }
                field = fieldGroup.buildAndBind(null, propertyId, fieldType);
            }

            if (!configuration.getFieldCaptions().isEmpty()) {
                field.setCaption(configuration.getFieldCaptions().get(i));
            } else {
                field.setCaption(SharedUtil.propertyIdToHumanFriendly(propertyId));
            }

            setDefaultConfiguration(field);
            fieldGroup.bind(field, propertyId);
            field.setReadOnly(readOnly);

            if (!configuration.getDisabledPropertyIds().isEmpty()) {
                field.setEnabled(!configuration.getDisabledPropertyIds().contains(propertyId));
            }

            FieldCreationListener creationListener = configuration.getFieldCreationListeners().get(propertyId);
            if (creationListener != null) {
                creationListener.fieldCreated(field);
            }

            fields.add(field);
        }

        if (!fields.isEmpty() && !readOnly) {
            fields.get(0).focus();
        }

        return fields;
    }

    protected void setDefaultConfiguration(Field<?> field) {
        if (field != null) {
            field.setWidth("100%");
            if (AbstractTextField.class.isAssignableFrom(field.getClass())) {
                ((AbstractTextField) field).setNullRepresentation("");
            }
        }
    }

    protected Button buildButton(CrudOperation operation, T domainObject, BeanFieldGroup fieldGroup, Button.ClickListener buttonClickListener) {
        Button button = new Button(buttonCaptions.get(operation), buttonIcons.get(operation));
        buttonStyleNames.get(operation).forEach(styleName -> button.addStyleName(styleName));
        button.addClickListener(event -> {
            try {
                fieldGroup.commit();
                buttonClickListener.buttonClick(event);

            } catch (FieldGroup.CommitException exception) {
                Notification.show(validationErrorMessage);
            }
        });
        return button;
    }

}
