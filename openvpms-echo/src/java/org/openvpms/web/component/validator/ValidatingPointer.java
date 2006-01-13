package org.openvpms.web.component.validator;

import java.util.List;

import org.apache.commons.jxpath.Pointer;

import org.openvpms.web.component.Validator;
import org.openvpms.web.component.dialog.ErrorDialog;

/**
 * Enter description here.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public class ValidatingPointer implements Pointer {

    /**
     * The pointer to delegate to.
     */
    private final Pointer _pointer;

    /**
     * The validator.
     */
    private final Validator _validator;


    /**
     * Construct a new <code>ValidatingPointer</code>.
     *
     * @param pointer   the pointer to delegate to
     * @param validator the validator
     */
    public ValidatingPointer(Pointer pointer, Validator validator) {
        _pointer = pointer;
        _validator = validator;
    }

    /**
     * Returns the value of the object, property or collection element this
     * pointer represents.
     */
    public Object getValue() {
        return _pointer.getValue();
    }

    /**
     * Returns the raw value of the object, property or collection element this
     * pointer represents.
     */
    public Object getNode() {
        return _pointer.getNode();
    }

    /**
     * Modifies the value of the object, property or collection element this
     * pointer represents.
     */
    public void setValue(Object value) {
        List<String> errors = _validator.validate(value);
        if (errors.isEmpty()) {
            _pointer.setValue(value);
        } else {
            ErrorDialog.show(errors.get(0));
        }
    }

    /**
     * Returns the node this pointer is based on.
     */
    public Object getRootNode() {
        return _pointer.getRootNode();
    }

    /**
     * Returns a string that is a proper "canonical" XPath that corresponds to
     * this pointer.
     */
    public String asPath() {
        return _pointer.asPath();
    }

    /**
     * In general, Pointers are cloneable. This implementation is not, and
     * throws <code>UnsupportedOperationException.</code>
     */
    public Object clone() {
        throw new UnsupportedOperationException();
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object
     *
     * @param object the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object is
     *         less than, equal to, or greater than the specified object.
     * @throws ClassCastException if the specified object's type prevents it
     *                            from being compared to this Object.
     */
    public int compareTo(Object object) {
        int result = -1;
        if (object instanceof ValidatingPointer) {
            ValidatingPointer other = (ValidatingPointer) object;
            result = _pointer.compareTo(other._pointer);
        }
        return result;
    }

}
