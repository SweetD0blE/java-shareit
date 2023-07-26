package ru.practicum.shareit.validation;

import javax.validation.groups.Default;

public interface GroupValidation {

    interface Create extends Default {}

    interface Update extends Default {}

}