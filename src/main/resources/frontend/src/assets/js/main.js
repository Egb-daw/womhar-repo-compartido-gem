import * as bootstrap from 'bootstrap';
import '../scss/styles.scss';

const THEME_KEY = 'womhat_theme';

const $ = (selector, root = document) => root.querySelector(selector);
const $$ = (selector, root = document) => Array.from(root.querySelectorAll(selector));

const VALIDATION_MESSAGES = {
    es: {
        required: 'Este campo es obligatorio.',
        email: 'Introduce un correo válido.',
        minlength: (value) => `Introduce al menos ${value} caracteres.`,
        maxlength: (value) => `No puede superar ${value} caracteres.`,
        pattern: 'El formato no es válido.',
        mismatch: 'Las contraseñas no coinciden.',
        number: 'Introduce un valor válido.',
        min: (value) => `El valor mínimo permitido es ${value}.`,
        max: (value) => `El valor máximo permitido es ${value}.`,
        checkbox: 'Debes marcar esta opción para continuar.'
    },
    en: {
        required: 'This field is required.',
        email: 'Enter a valid email address.',
        minlength: (value) => `Enter at least ${value} characters.`,
        maxlength: (value) => `Must not exceed ${value} characters.`,
        pattern: 'Invalid format.',
        mismatch: 'Passwords do not match.',
        number: 'Enter a valid value.',
        min: (value) => `The minimum allowed value is ${value}.`,
        max: (value) => `The maximum allowed value is ${value}.`,
        checkbox: 'You must check this option to continue.'
    }
};

function getSavedTheme() {
    try {
        return localStorage.getItem(THEME_KEY) || 'dark';
    } catch (error) {
        return 'dark';
    }
}

function applyTheme(theme) {
    document.documentElement.setAttribute('data-bs-theme', theme);
    document.documentElement.style.colorScheme = theme === 'light' ? 'light' : 'dark';

    try {
        localStorage.setItem(THEME_KEY, theme);
    } catch (error) {
        console.warn('No se pudo guardar el tema.', error);
    }

    const icon = $('#themeIcon');
    if (icon) {
        icon.className = theme === 'light' ? 'bi bi-sun' : 'bi bi-moon-stars';
    }
}

window.setTheme = applyTheme;

function initTheme() {
    applyTheme(getSavedTheme());
}

function initHeaderInteractions() {
    $$('[data-bs-toggle="dropdown"]').forEach((trigger) => {
        bootstrap.Dropdown.getOrCreateInstance(trigger);
    });

    $$('.navbar-toggler[data-bs-target]').forEach((trigger) => {
        const targetSelector = trigger.dataset.bsTarget || trigger.getAttribute('data-bs-target');
        if (!targetSelector) return;

        const target = document.querySelector(targetSelector);
        if (!target) return;

        bootstrap.Collapse.getOrCreateInstance(target, { toggle: false });
    });
}

function bindThemeOptions() {
    $$('.js-theme-option').forEach((button) => {
        button.addEventListener('click', () => {
            const theme = button.dataset.themeValue;
            if (!theme) return;
            applyTheme(theme);
        });
    });
}

function getValidationLocale() {
    const lang = document.documentElement.lang?.toLowerCase();
    return lang?.startsWith('en') ? 'en' : 'es';
}

function getValidationMessage(key, value = null) {
    const locale = getValidationLocale();
    const catalog = VALIDATION_MESSAGES[locale] || VALIDATION_MESSAGES.es;
    const entry = catalog[key];
    return typeof entry === 'function' ? entry(value) : entry;
}

function getValidationContainer(field) {
    return field.closest('.auth-field')
        || field.closest('.form-group')
        || field.closest('[class*="col-"]')
        || field.parentElement;
}

function getFeedbackNode(field) {
    const container = getValidationContainer(field);
    if (!container) return null;

    let feedback = $('.js-inline-error', container);
    if (feedback) return feedback;

    feedback = document.createElement('div');
    feedback.className = 'invalid-feedback d-block js-inline-error';
    container.appendChild(feedback);
    return feedback;
}

function rememberBaseDescribedBy(field) {
    if (!field || field.dataset.baseDescribedby !== undefined) {
        return;
    }

    field.dataset.baseDescribedby = field.getAttribute('aria-describedby') || '';
}

function setFieldDescribedBy(field, extraIds = []) {
    if (!field) return;

    rememberBaseDescribedBy(field);

    const baseIds = (field.dataset.baseDescribedby || '')
        .split(/\s+/)
        .filter(Boolean);

    const ids = [...new Set([...baseIds, ...extraIds.filter(Boolean)])];

    if (ids.length > 0) {
        field.setAttribute('aria-describedby', ids.join(' '));
        return;
    }

    field.removeAttribute('aria-describedby');
}

function clearFieldError(field) {
    field.classList.remove('is-invalid');
    field.removeAttribute('aria-invalid');

    const container = getValidationContainer(field);
    if (container) {
        $$('.invalid-feedback', container)
            .filter((node) => !node.classList.contains('js-inline-error'))
            .forEach((node) => {
                node.hidden = true;
            });
    }

    const feedback = container ? $('.js-inline-error', container) : null;
    if (feedback) {
        feedback.remove();
    }

    setFieldDescribedBy(field);
}

function showFieldError(field, message) {
    field.classList.add('is-invalid');
    field.setAttribute('aria-invalid', 'true');

    const container = getValidationContainer(field);
    if (container) {
        $$('.invalid-feedback', container)
            .filter((node) => !node.classList.contains('js-inline-error'))
            .forEach((node) => {
                node.hidden = true;
            });
    }

    const feedback = getFeedbackNode(field);
    if (!feedback) return;

    if (!feedback.id) {
        feedback.id = field.id ? `${field.id}Error` : `fieldError${Math.random().toString(36).slice(2, 8)}`;
    }

    feedback.textContent = message;
    setFieldDescribedBy(field, [feedback.id]);
}

function syncServerValidationState() {
    $$('input.is-invalid, select.is-invalid, textarea.is-invalid').forEach((field) => {
        field.setAttribute('aria-invalid', 'true');

        const container = getValidationContainer(field);
        if (!container) return;

        const feedback = $$('.invalid-feedback', container)
            .find((node) => !node.classList.contains('js-inline-error') && node.textContent.trim().length > 0);

        if (!feedback) {
            rememberBaseDescribedBy(field);
            return;
        }

        if (!feedback.id) {
            feedback.id = field.id
                ? `${field.id}Error`
                : `fieldError${Math.random().toString(36).slice(2, 8)}`;
        }

        setFieldDescribedBy(field, [feedback.id]);
    });
}

function getFieldValidationMessage(field) {
    if (field.validity.valueMissing) {
        return field.type === 'checkbox'
            ? getValidationMessage('checkbox')
            : getValidationMessage('required');
    }

    if (field.validity.typeMismatch && field.type === 'email') {
        return getValidationMessage('email');
    }

    if (field.validity.tooShort && field.minLength > 0) {
        return getValidationMessage('minlength', field.minLength);
    }

    if (field.validity.tooLong && field.maxLength > 0) {
        return getValidationMessage('maxlength', field.maxLength);
    }

    if (field.validity.rangeUnderflow && field.min !== '') {
        return getValidationMessage('min', field.min);
    }

    if (field.validity.rangeOverflow && field.max !== '') {
        return getValidationMessage('max', field.max);
    }

    if (field.validity.badInput) {
        return getValidationMessage('number');
    }

    if (field.validity.patternMismatch) {
        return field.dataset.validationPatternMsg || getValidationMessage('pattern');
    }

    return field.validationMessage || getValidationMessage('required');
}

function validateField(field) {
    if (!field || field.disabled || field.type === 'hidden') {
        return true;
    }

    clearFieldError(field);

    if (field.checkValidity()) {
        return true;
    }

    showFieldError(field, getFieldValidationMessage(field));
    return false;
}

function validatePasswordConfirmation(form) {
    const passwordField = $('#regPass, #newPassword, input[name="password"], input[name="newPassword"]', form);
    const confirmField = $('#confirmPassword, input[name="confirmPassword"]', form);

    if (!passwordField || !confirmField) {
        return true;
    }

    clearFieldError(confirmField);

    if (!confirmField.value) {
        return validateField(confirmField);
    }

    if (passwordField.value !== confirmField.value) {
        showFieldError(confirmField, getValidationMessage('mismatch'));
        return false;
    }

    return true;
}

function normalizeToken(value) {
    return (value || '').trim().toUpperCase();
}

function getDeactivationTokens(field) {
    return (field.dataset.confirmTokens || '')
        .split('|')
        .map(normalizeToken)
        .filter(Boolean);
}

function validateAccountDeactivationField(field) {
    if (!field) {
        return true;
    }

    const allowedTokens = getDeactivationTokens(field);
    if (allowedTokens.length === 0) {
        field.setCustomValidity('');
        return validateField(field);
    }

    const normalizedValue = normalizeToken(field.value);
    const requiredMessage = field.dataset.confirmRequiredMsg || getValidationMessage('required');
    const invalidMessage = field.dataset.confirmInvalidMsg || getValidationMessage('required');

    if (!normalizedValue) {
        field.setCustomValidity(requiredMessage);
    } else if (allowedTokens.includes(normalizedValue)) {
        field.setCustomValidity('');
    } else {
        field.setCustomValidity(invalidMessage);
    }

    return validateField(field);
}

function bindAccountDeactivationValidation() {
    $$('form[data-account-deactivation-form="true"]').forEach((form) => {
        form.setAttribute('novalidate', 'novalidate');

        const confirmField = $('#deactivationConfirmText', form);
        if (!confirmField) return;

        const handler = () => {
            validateAccountDeactivationField(confirmField);
        };

        confirmField.addEventListener('input', handler);
        confirmField.addEventListener('blur', handler);
        confirmField.addEventListener('change', handler);

        form.addEventListener('submit', (event) => {
            const isValid = validateAccountDeactivationField(confirmField);
            if (isValid) {
                return;
            }

            event.preventDefault();
            event.stopPropagation();
            focusFirstInvalidField(form);
        });
    });
}

function bindCustomValidation() {
    $$('form[data-womhat-validate="true"]').forEach((form) => {
        form.setAttribute('novalidate', 'novalidate');

        $$('input, select, textarea', form).forEach((field) => {
            if (field.type === 'hidden' || field.disabled) return;

            const handler = () => {
                validateField(field);

                if (field.id === 'regPass' || field.id === 'newPassword' || field.id === 'confirmPassword') {
                    validatePasswordConfirmation(form);
                }
            };

            field.addEventListener('input', handler);
            field.addEventListener('change', handler);
            field.addEventListener('blur', handler);
        });

        form.addEventListener('submit', (event) => {
            let valid = true;

            $$('input, select, textarea', form).forEach((field) => {
                if (!validateField(field)) {
                    valid = false;
                }
            });

            if (!validatePasswordConfirmation(form)) {
                valid = false;
            }

            if (!valid) {
                event.preventDefault();
                event.stopPropagation();
                focusFirstInvalidField(form);
            }
        });
    });
}

function initSmoothScroll() {
    $$('a[href^="#"]').forEach((anchor) => {
        anchor.addEventListener('click', (event) => {
            const href = anchor.getAttribute('href');
            if (!href || href === '#') return;

            const target = document.querySelector(href);
            if (!target) return;

            event.preventDefault();
            target.scrollIntoView({
                behavior: 'smooth',
                block: 'start'
            });
        });
    });
}

function initBackToTop() {
    const button = $('#backToTop');
    if (!button) return;

    const toggle = () => {
        button.classList.toggle('show', window.scrollY > 420);
    };

    button.addEventListener('click', () => {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    });

    window.addEventListener('scroll', toggle, { passive: true });
    toggle();
}

function initFlashMessages() {
    $$('[data-autohide="true"]').forEach((alertEl) => {
        window.setTimeout(() => {
            try {
                bootstrap.Alert.getOrCreateInstance(alertEl).close();
            } catch (error) {
                alertEl.remove();
            }
        }, 4500);
    });
}

function bindMediaSkeletons() {
    $$('.js-media-skeleton').forEach((container) => {
        const media = $('img', container);
        if (!media) return;

        const markLoaded = () => {
            container.classList.remove('is-pending');
            container.classList.add('is-loaded');
        };

        if (media.complete && media.naturalWidth > 0) {
            markLoaded();
            return;
        }

        media.addEventListener('load', markLoaded, { once: true });
        media.addEventListener('error', markLoaded, { once: true });
    });
}

function bindProfileImagePreview() {
    const fileInput = $('#profileImageFile');
    const preview = $('#profileAvatarPreview');

    if (!fileInput || !preview) return;

    fileInput.addEventListener('change', (event) => {
        const file = event.target.files?.[0];
        if (!file) return;

        const reader = new FileReader();
        reader.onload = (loadEvent) => {
            if (loadEvent.target?.result) {
                preview.src = loadEvent.target.result;
            }
        };
        reader.readAsDataURL(file);
    });
}

function bindResetPasswordValidation() {
    const newPass = $('#newPassword');
    const confPass = $('#confirmPassword');

    if (!newPass || !confPass) return;

    const checkPasswords = () => {
        const val1 = newPass.value.trim();
        const val2 = confPass.value.trim();

        if (val2.length === 0) {
            clearFieldError(confPass);
            return;
        }

        if (val1 === val2) {
            clearFieldError(confPass);
        } else {
            showFieldError(confPass, getValidationMessage('mismatch'));
        }
    };

    newPass.addEventListener('input', checkPasswords);
    confPass.addEventListener('input', checkPasswords);
    checkPasswords();
}

function bindPasswordToggles() {
    $$('[data-password-toggle]').forEach((button) => {
        button.setAttribute('aria-pressed', 'false');

        button.addEventListener('click', () => {
            const targetId = button.dataset.passwordToggle;
            const input = document.getElementById(targetId);
            const icon = $('i', button);

            if (!input) return;

            const isPassword = input.type === 'password';
            input.type = isPassword ? 'text' : 'password';

            if (icon) {
                icon.className = isPassword ? 'bi bi-eye-slash' : 'bi bi-eye';
            }

            button.setAttribute('aria-pressed', isPassword ? 'true' : 'false');
        });
    });
}

function bindEquipmentFormSections() {
    const typeSelect = $('#equipmentType');
    if (!typeSelect) return;

    const sections = {
        host: $('[data-equipment-section="host"]'),
        network: $('[data-equipment-section="network"]'),
        storage: $('[data-equipment-section="storage"]')
    };

    const visibilityMap = {
        SERVER: ['host'],
        SWITCH: ['network'],
        ROUTER: ['network'],
        FIREWALL: ['network'],
        NAS: ['storage'],
        STORAGE: ['storage']
    };

    const updateSections = () => {
        const visibleSections = new Set(visibilityMap[typeSelect.value] || []);

        Object.entries(sections).forEach(([sectionName, element]) => {
            if (!element) return;
            element.hidden = !visibleSections.has(sectionName);
        });
    };

    typeSelect.addEventListener('change', updateSections);
    updateSections();
}

function bindConfirmations() {
    $$('form[data-confirm]').forEach((form) => {
        form.addEventListener('submit', (event) => {
            const message = form.dataset.confirm;
            if (!message) return;

            if (!window.confirm(message)) {
                event.preventDefault();
            }
        });
    });
}

function bindPrintActions() {
    $$('[data-print-report]').forEach((button) => {
        button.addEventListener('click', (event) => {
            event.preventDefault();
            window.print();
        });
    });
}

function bindSubmitOnce() {
    // Previene doble envío en operaciones sensibles (delete, create/update)
    // sin bloquear formularios con validación fallida.
    $$('form[data-disable-on-submit="true"]').forEach((form) => {
        form.addEventListener('submit', (event) => {
            if (form.dataset.submitted === 'true') {
                event.preventDefault();
                return;
            }

            if (event.defaultPrevented || !form.checkValidity() || form.querySelector('.is-invalid')) {
                return;
            }

            form.dataset.submitted = 'true';

            $$('button[type="submit"], input[type="submit"]', form).forEach((button) => {
                button.disabled = true;
                button.classList.add('is-submitting');
                button.setAttribute('aria-disabled', 'true');
            });
        });
    });
}

function focusFirstInvalidField(root = document) {
    const invalidField = $('.is-invalid, [aria-invalid="true"]', root);
    if (!invalidField) return;

    window.requestAnimationFrame(() => {
        invalidField.scrollIntoView({
            behavior: 'smooth',
            block: 'center'
        });

        if (typeof invalidField.focus === 'function') {
            invalidField.focus({ preventScroll: true });
        }
    });
}

function bindCharacterCounters() {
    $$('[data-char-counter]').forEach((field) => {
        const counterId = field.dataset.charCounter;
        const counter = document.getElementById(counterId);

        if (!counter) return;

        const maxLength = Number(field.getAttribute('maxlength')) || 0;

        const render = () => {
            const currentLength = field.value?.length ?? 0;
            counter.textContent = maxLength > 0
                ? `${currentLength} / ${maxLength}`
                : `${currentLength}`;
        };

        field.addEventListener('input', render);
        render();
    });
}

function bindCatalogFilterAutoSubmit() {
    $$('[data-catalog-auto-submit="true"]').forEach((field) => {
        field.addEventListener('change', () => {
            const form = field.closest('form');
            if (!form) return;

            if (typeof form.requestSubmit === 'function') {
                form.requestSubmit();
            } else {
                form.submit();
            }
        });
    });
}

function bindCatalogOrderPreview() {
    const form = $('.js-catalog-order-form');
    if (!form) return;

    const quantityField = $('#quantity', form);
    const quantityOutput = $('#catalogOrderQuantity');
    const totalOutput = $('#catalogEstimatedTotal');

    const unitPrice = Number(form.dataset.unitPrice || '0');
    const currency = form.dataset.currency || '€';

    const render = () => {
        const rawValue = Number(quantityField?.value || 1);
        const quantity = Number.isFinite(rawValue) && rawValue > 0 ? rawValue : 1;

        if (quantityField) {
            quantityField.value = quantity;
        }

        if (quantityOutput) {
            quantityOutput.textContent = String(quantity);
        }

        if (totalOutput) {
            if (unitPrice > 0) {
                totalOutput.textContent = `${(unitPrice * quantity).toFixed(2)} ${currency}`;
            } else {
                totalOutput.textContent = '—';
            }
        }
    };

    quantityField?.addEventListener('input', render);
    quantityField?.addEventListener('change', render);

    render();
}

document.addEventListener('DOMContentLoaded', () => {
    initTheme();
    initHeaderInteractions();
    bindThemeOptions();
    syncServerValidationState();
    bindCustomValidation();

    initSmoothScroll();
    initBackToTop();
    initFlashMessages();

    bindMediaSkeletons();
    bindProfileImagePreview();
    focusFirstInvalidField();
    bindCharacterCounters();

    bindResetPasswordValidation();
    bindAccountDeactivationValidation();
    bindPasswordToggles();
    bindEquipmentFormSections();
    bindConfirmations();
    bindPrintActions();
    bindSubmitOnce();

    bindCatalogFilterAutoSubmit();
    bindCatalogOrderPreview();
});
