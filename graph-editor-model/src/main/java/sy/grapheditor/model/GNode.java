/**
 */
package sy.grapheditor.model;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import java.util.Map;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>GNode</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link GNode#getId <em>Id</em>}</li>
 *   <li>{@link GNode#getType <em>Type</em>}</li>
 *   <li>{@link GNode#getX <em>X</em>}</li>
 *   <li>{@link GNode#getY <em>Y</em>}</li>
 *   <li>{@link GNode#getWidth <em>Width</em>}</li>
 *   <li>{@link GNode#getHeight <em>Height</em>}</li>
 *   <li>{@link GNode#getConnectors <em>Connectors</em>}</li>
 * </ul>
 *
 * @see GraphPackage#getGNode()
 * @model
 * @generated
 */
public interface GNode extends EObject {
	/**
	 * Returns the value of the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Id</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Id</em>' attribute.
	 * @see #setId(long)
	 * @see GraphPackage#getGNode_Id()
	 * @model id="true"
	 * @generated
	 */
	long getId();

	/**
	 * Sets the value of the '{@link GNode#getId <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Id</em>' attribute.
	 * @see #getId()
	 * @generated
	 */
	void setId(long value);

	/**
	 * Returns the value of the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Type</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type</em>' attribute.
	 * @see #setType(String)
	 * @see GraphPackage#getGNode_Type()
	 * @model
	 * @generated
	 */
	String getType();

	/**
	 * Sets the value of the '{@link GNode#getType <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' attribute.
	 * @see #getType()
	 * @generated
	 */
	void setType(String value);

	/**
	 * Returns the value of the '<em><b>X</b></em>' attribute.
	 * The default value is <code>"0"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>X</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>X</em>' attribute.
	 * @see #setX(double)
	 * @see GraphPackage#getGNode_X()
	 * @model default="0" required="true"
	 * @generated
	 */
	double getX();

	/**
	 * Sets the value of the '{@link GNode#getX <em>X</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>X</em>' attribute.
	 * @see #getX()
	 * @generated
	 */
	void setX(double value);

	/**
	 * Returns the value of the '<em><b>Y</b></em>' attribute.
	 * The default value is <code>"0"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Y</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Y</em>' attribute.
	 * @see #setY(double)
	 * @see GraphPackage#getGNode_Y()
	 * @model default="0" required="true"
	 * @generated
	 */
	double getY();

	/**
	 * Sets the value of the '{@link GNode#getY <em>Y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Y</em>' attribute.
	 * @see #getY()
	 * @generated
	 */
	void setY(double value);

	/**
	 * Returns the value of the '<em><b>Width</b></em>' attribute.
	 * The default value is <code>"151"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Width</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Width</em>' attribute.
	 * @see #setWidth(double)
	 * @see GraphPackage#getGNode_Width()
	 * @model default="151" required="true"
	 * @generated
	 */
	double getWidth();

	/**
	 * Sets the value of the '{@link GNode#getWidth <em>Width</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Width</em>' attribute.
	 * @see #getWidth()
	 * @generated
	 */
	void setWidth(double value);

	/**
	 * Returns the value of the '<em><b>Height</b></em>' attribute.
	 * The default value is <code>"101"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Height</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Height</em>' attribute.
	 * @see #setHeight(double)
	 * @see GraphPackage#getGNode_Height()
	 * @model default="101" required="true"
	 * @generated
	 */
	double getHeight();

	/**
	 * Sets the value of the '{@link GNode#getHeight <em>Height</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Height</em>' attribute.
	 * @see #getHeight()
	 * @generated
	 */
	void setHeight(double value);

	/**
	 * Returns the value of the '<em><b>Connectors</b></em>' containment reference list.
	 * The list contents are of type {@link GConnector}.
	 * It is bidirectional and its opposite is '{@link GConnector#getParent <em>Parent</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Connectors</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Connectors</em>' containment reference list.
	 * @see GraphPackage#getGNode_Connectors()
	 * @see GConnector#getParent
	 * @model opposite="parent" containment="true"
	 * @generated
	 */
	EList<GConnector> getConnectors();

	String getProcessorClassName();

	void setProcessorClassName(String value);

	String getProcessorJson();

	void setProcessorJson(String value);

	Map<Integer, Object> getAttachments();

	void clearAttachments();

	Object getAttachment(Integer attachmentNum);

	void setAttachment(Integer attachmentNum, Object attachment);

	double getSelectedHeight();

} // GNode
