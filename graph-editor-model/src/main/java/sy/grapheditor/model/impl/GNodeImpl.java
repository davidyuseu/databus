/**
 *
 */
package sy.grapheditor.model.impl;

import sy.grapheditor.model.GConnector;
import sy.grapheditor.model.GNode;
import sy.grapheditor.model.GraphPackage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>GNode</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link GNodeImpl#getId <em>Id</em>}</li>
 *   <li>{@link GNodeImpl#getType <em>Type</em>}</li>
 *   <li>{@link GNodeImpl#getX <em>X</em>}</li>
 *   <li>{@link GNodeImpl#getY <em>Y</em>}</li>
 *   <li>{@link GNodeImpl#getWidth <em>Width</em>}</li>
 *   <li>{@link GNodeImpl#getHeight <em>Height</em>}</li>
 *   <li>{@link GNodeImpl#getConnectors <em>Connectors</em>}</li>
 * </ul>
 *
 * @generated
 */
public class GNodeImpl extends MinimalEObjectImpl.Container implements GNode {
    /**
     * The default value of the '{@link #getId() <em>Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getId()
     * @generated
     * @ordered
     */
    protected static final long ID_EDEFAULT = 0L;

    /**
     * The cached value of the '{@link #getId() <em>Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getId()
     * @generated
     * @ordered
     */
    protected long id = ID_EDEFAULT;

    /**
     * The default value of the '{@link #getType() <em>Type</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getType()
     * @generated
     * @ordered
     */
    protected static final String TYPE_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getType() <em>Type</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getType()
     * @generated
     * @ordered
     */
    protected String type = TYPE_EDEFAULT;

    /**
     * The default value of the '{@link #getX() <em>X</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getX()
     * @generated
     * @ordered
     */
    protected static final double X_EDEFAULT = 0.0;

    /**
     * The cached value of the '{@link #getX() <em>X</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getX()
     * @generated
     * @ordered
     */
    protected double x = X_EDEFAULT;

    /**
     * The default value of the '{@link #getY() <em>Y</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getY()
     * @generated
     * @ordered
     */
    protected static final double Y_EDEFAULT = 0.0;

    /**
     * The cached value of the '{@link #getY() <em>Y</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getY()
     * @generated
     * @ordered
     */
    protected double y = Y_EDEFAULT;

    /**
     * The default value of the '{@link #getWidth() <em>Width</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getWidth()
     * @generated
     * @ordered
     */
    protected static final double WIDTH_EDEFAULT = 151.0;

    /**
     * The cached value of the '{@link #getWidth() <em>Width</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getWidth()
     * @generated
     * @ordered
     */
    protected double width = WIDTH_EDEFAULT;

    /**
     * The default value of the '{@link #getHeight() <em>Height</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getHeight()
     * @generated
     * @ordered
     */
    protected static final double HEIGHT_EDEFAULT = 101.0;

    /**
     * The cached value of the '{@link #getHeight() <em>Height</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getHeight()
     * @generated
     * @ordered
     */
    protected double height = HEIGHT_EDEFAULT;

    /**
     * The cached value of the '{@link #getConnectors() <em>Connectors</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getConnectors()
     * @generated
     * @ordered
     */
    protected EList<GConnector> connectors;

    protected static final String PROCESSOR_CLASS_NAME_EDEFAULT = "null";

    protected String processorClassName = PROCESSOR_CLASS_NAME_EDEFAULT;

    protected static final String PROCESSOR_JSON_EDEFAULT = "null";

    protected String processorJson = PROCESSOR_JSON_EDEFAULT;

    protected Map<Integer, Object> attachments = null;


    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected GNodeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return GraphPackage.Literals.GNODE;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public long getId() {
        return id;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setId(long newId) {
        long oldId = id;
        id = newId;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, GraphPackage.GNODE__ID, oldId, id));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getType() {
        return type;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setType(String newType) {
        String oldType = type;
        type = newType;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, GraphPackage.GNODE__TYPE, oldType, type));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public double getX() {
        return x;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setX(double newX) {
        double oldX = x;
        x = newX;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, GraphPackage.GNODE__X, oldX, x));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public double getY() {
        return y;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setY(double newY) {
        double oldY = y;
        y = newY;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, GraphPackage.GNODE__Y, oldY, y));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public double getWidth() {
        return width;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setWidth(double newWidth) {
        double oldWidth = width;
        width = newWidth;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, GraphPackage.GNODE__WIDTH, oldWidth, width));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public double getHeight() {
        return height;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setHeight(double newHeight) {
        double oldHeight = height;
        height = newHeight;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, GraphPackage.GNODE__HEIGHT, oldHeight, height));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList<GConnector> getConnectors() {
        if (connectors == null) {
            connectors = new EObjectContainmentWithInverseEList<GConnector>(GConnector.class, this, GraphPackage.GNODE__CONNECTORS, GraphPackage.GCONNECTOR__PARENT);
        }
        return connectors;
    }

    @Override
    public String getProcessorJson() {
        return processorJson;
    }

    @Override
    public void setProcessorJson(String newProcessorJson) {
        String oldProcessorJson = processorJson;
        processorJson = newProcessorJson;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, GraphPackage.GNODE__PROCESSOR_JSON, oldProcessorJson, processorJson));
    }

    public static final Integer PROCESSOR = 0;
    public static final Integer PROCESSOR_PANE = 1;

    @Override
    public synchronized Map<Integer, Object> getAttachments() {
        return attachments;
    }

    @Override
    public synchronized Object getAttachment(Integer attachmentNum) {
        return attachments.get(attachmentNum);
    }

    @Override
    public synchronized void setAttachment(Integer attachmentNum, Object attachment) {
        if(attachments == null)
            attachments = new HashMap<>();
        attachments.put(attachmentNum, attachment);
    }

    @Override
    public synchronized void clearAttachments() {
        if(attachments != null) {
            attachments.clear();
            attachments = null;
        }
    }

    @Override
    public double getSelectedHeight() {
        return getHeight();
    }

    @Override
    public String getProcessorClassName() {
        return processorClassName;
    }

    @Override
    public void setProcessorClassName(String newProcessorClassName) {
        String oldProcessorClassName = processorClassName;
        processorClassName = newProcessorClassName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, GraphPackage.GNODE__PROCESSOR_CLASS_NAME, oldProcessorClassName, processorClassName));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @SuppressWarnings("unchecked")
    @Override
    public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case GraphPackage.GNODE__CONNECTORS:
                return ((InternalEList<InternalEObject>) (InternalEList<?>) getConnectors()).basicAdd(otherEnd, msgs);
        }
        return super.eInverseAdd(otherEnd, featureID, msgs);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case GraphPackage.GNODE__CONNECTORS:
                return ((InternalEList<?>) getConnectors()).basicRemove(otherEnd, msgs);
        }
        return super.eInverseRemove(otherEnd, featureID, msgs);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case GraphPackage.GNODE__ID:
                return getId();
            case GraphPackage.GNODE__TYPE:
                return getType();
            case GraphPackage.GNODE__X:
                return getX();
            case GraphPackage.GNODE__Y:
                return getY();
            case GraphPackage.GNODE__WIDTH:
                return getWidth();
            case GraphPackage.GNODE__HEIGHT:
                return getHeight();
            case GraphPackage.GNODE__CONNECTORS:
                return getConnectors();
            case GraphPackage.GNODE__PROCESSOR_CLASS_NAME:
                return getProcessorClassName();
            case GraphPackage.GNODE__PROCESSOR_JSON:
                return getProcessorJson();
        }
        return super.eGet(featureID, resolve, coreType);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @SuppressWarnings("unchecked")
    @Override
    public void eSet(int featureID, Object newValue) {
        switch (featureID) {
            case GraphPackage.GNODE__ID:
                setId((long) newValue);
                return;
            case GraphPackage.GNODE__TYPE:
                setType((String) newValue);
                return;
            case GraphPackage.GNODE__X:
                setX((Double) newValue);
                return;
            case GraphPackage.GNODE__Y:
                setY((Double) newValue);
                return;
            case GraphPackage.GNODE__WIDTH:
                setWidth((Double) newValue);
                return;
            case GraphPackage.GNODE__HEIGHT:
                setHeight((Double) newValue);
                return;
            case GraphPackage.GNODE__CONNECTORS:
                getConnectors().clear();
                getConnectors().addAll((Collection<? extends GConnector>) newValue);
                return;
            case GraphPackage.GNODE__PROCESSOR_CLASS_NAME:
                setProcessorClassName((String) newValue);
            case GraphPackage.GNODE__PROCESSOR_JSON:
                setProcessorJson((String) newValue);
                return;
        }
        super.eSet(featureID, newValue);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public void eUnset(int featureID) {
        switch (featureID) {
            case GraphPackage.GNODE__ID:
                setId(ID_EDEFAULT);
                return;
            case GraphPackage.GNODE__TYPE:
                setType(TYPE_EDEFAULT);
                return;
            case GraphPackage.GNODE__X:
                setX(X_EDEFAULT);
                return;
            case GraphPackage.GNODE__Y:
                setY(Y_EDEFAULT);
                return;
            case GraphPackage.GNODE__WIDTH:
                setWidth(WIDTH_EDEFAULT);
                return;
            case GraphPackage.GNODE__HEIGHT:
                setHeight(HEIGHT_EDEFAULT);
                return;
            case GraphPackage.GNODE__CONNECTORS:
                getConnectors().clear();
                return;
            case GraphPackage.GNODE__PROCESSOR_CLASS_NAME:
                setProcessorClassName(PROCESSOR_CLASS_NAME_EDEFAULT);
            case GraphPackage.GNODE__PROCESSOR_JSON:
                setProcessorJson(PROCESSOR_JSON_EDEFAULT);
                return;
        }
        super.eUnset(featureID);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public boolean eIsSet(int featureID) {
        switch (featureID) {
            case GraphPackage.GNODE__ID:
                return ID_EDEFAULT == 0L ? id != 0L : !(ID_EDEFAULT == id);
            case GraphPackage.GNODE__TYPE:
                return TYPE_EDEFAULT == null ? type != null : !TYPE_EDEFAULT.equals(type);
            case GraphPackage.GNODE__X:
                return x != X_EDEFAULT;
            case GraphPackage.GNODE__Y:
                return y != Y_EDEFAULT;
            case GraphPackage.GNODE__WIDTH:
                return width != WIDTH_EDEFAULT;
            case GraphPackage.GNODE__HEIGHT:
                return height != HEIGHT_EDEFAULT;
            case GraphPackage.GNODE__CONNECTORS:
                return connectors != null && !connectors.isEmpty();
            case GraphPackage.GNODE__PROCESSOR_CLASS_NAME:
                return PROCESSOR_CLASS_NAME_EDEFAULT == null ? processorClassName != null : !PROCESSOR_CLASS_NAME_EDEFAULT.equals(processorClassName);
            case GraphPackage.GNODE__PROCESSOR_JSON:
                return PROCESSOR_JSON_EDEFAULT == null ? processorJson != null : !PROCESSOR_JSON_EDEFAULT.equals(processorJson);
        }
        return super.eIsSet(featureID);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public String toString() {
        if (eIsProxy()) return super.toString();

        StringBuffer result = new StringBuffer(super.toString());
        result.append(" (id: ");
        result.append(id);
        result.append(", type: ");
        result.append(type);
        result.append(", x: ");
        result.append(x);
        result.append(", y: ");
        result.append(y);
        result.append(", width: ");
        result.append(width);
        result.append(", height: ");
        result.append(height);
        result.append(", processorJson: ");
        result.append(processorJson);
        result.append(')');
        return result.toString();
    }

} //GNodeImpl
