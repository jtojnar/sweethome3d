/*
 * HomeCameraTest.java 21 juin 2007
 * 
 * Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.eteks.sweethome3d.junit;

import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;

import junit.extensions.abbot.ComponentTestFixture;
import abbot.finder.AWTHierarchy;
import abbot.finder.BasicFinder;
import abbot.finder.ComponentSearchException;
import abbot.finder.matchers.ClassMatcher;
import abbot.finder.matchers.WindowMatcher;
import abbot.tester.ComponentLocation;
import abbot.tester.JComponentTester;

import com.eteks.sweethome3d.io.DefaultUserPreferences;
import com.eteks.sweethome3d.model.Camera;
import com.eteks.sweethome3d.model.CatalogTexture;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.ObserverCamera;
import com.eteks.sweethome3d.model.TextureImage;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.swing.ColorButton;
import com.eteks.sweethome3d.swing.Home3DAttributesPanel;
import com.eteks.sweethome3d.swing.HomeComponent3D;
import com.eteks.sweethome3d.swing.HomePane;
import com.eteks.sweethome3d.swing.PlanComponent;
import com.eteks.sweethome3d.swing.SwingViewFactory;
import com.eteks.sweethome3d.swing.TextureChoiceComponent;
import com.eteks.sweethome3d.viewcontroller.Home3DAttributesController;
import com.eteks.sweethome3d.viewcontroller.HomeController;

/**
 * Tests camera changes in home.
 * @author Emmanuel Puybaret
 */
public class HomeCameraTest extends ComponentTestFixture {
  public void testHomeCamera() throws ComponentSearchException, InterruptedException, 
      NoSuchFieldException, IllegalAccessException, InvocationTargetException {
    Locale.setDefault(Locale.FRANCE);
    UserPreferences preferences = new DefaultUserPreferences();
    Home home = new Home();
    final HomeController controller = 
        new HomeController(home, preferences, new SwingViewFactory());
    JComponent homeView = (JComponent)controller.getView();
    PlanComponent planComponent = (PlanComponent)TestUtilities.findComponent(
        homeView, PlanComponent.class);
    HomeComponent3D component3D = (HomeComponent3D)TestUtilities.findComponent(
        homeView, HomeComponent3D.class);

    // 1. Create a frame that displays a home view 
    JFrame frame = new JFrame("Home Camera Test");    
    frame.add(homeView);
    frame.pack();

    // Show home plan frame
    showWindow(frame);
    JComponentTester tester = new JComponentTester();
    tester.waitForIdle();
    // Transfer focus to plan view 
    planComponent.requestFocusInWindow();
    tester.waitForIdle();
    
    // Check plan view has focus
    assertTrue("Plan component doesn't have the focus", planComponent.isFocusOwner());
    // Check default camera is the top camera
    assertSame("Default camera isn't top camera", 
        home.getTopCamera(), home.getCamera());
    // Check default camera location and angles
    assertCoordinatesAndAnglesEqualCameraLocationAndAngles(500, 1500, 1000, 
        (float)Math.PI, (float)Math.PI / 4, home.getCamera());
    
    // 2. Create one wall between points (50, 50) and (150, 50) at a bigger scale
    runAction(controller, HomePane.ActionType.CREATE_WALLS);
    runAction(controller, HomePane.ActionType.ZOOM_IN);
    tester.actionKeyPress(KeyEvent.VK_SHIFT);
    tester.actionClick(planComponent, 50, 50);
    tester.actionClick(planComponent, 150, 50, InputEvent.BUTTON1_MASK, 2);
    tester.actionKeyRelease(KeyEvent.VK_SHIFT);
    // Check wall length is 100 * plan scale
    Wall wall = home.getWalls().iterator().next();
    assertTrue("Incorrect wall length " + 100 / planComponent.getScale() 
               + " " + (wall.getXEnd() - wall.getXStart()), 
        Math.abs(wall.getXEnd() - wall.getXStart() - 100 / planComponent.getScale()) < 1E-3);
    float xWallMiddle = (wall.getXEnd() + wall.getXStart()) / 2;
    float yWallMiddle = (wall.getYEnd() + wall.getYStart()) / 2;
    // Check camera location and angles
    assertCoordinatesAndAnglesEqualCameraLocationAndAngles(xWallMiddle, yWallMiddle + 1000, 1000, 
        (float)Math.PI, (float)Math.PI / 4, home.getCamera());
    
    // 3. Transfer focus to 3D view with TAB key 
    tester.actionKeyStroke(KeyEvent.VK_TAB);
    // Check 3D view has focus
    assertTrue("3D component doesn't have the focus", component3D.isFocusOwner());
    // Add 1� to camera pitch
    tester.actionKeyStroke(KeyEvent.VK_PAGE_UP);
    // Check camera location and angles
    assertCoordinatesAndAnglesEqualCameraLocationAndAngles(xWallMiddle, 1009.0617f, 1017.3002f, 
        (float)Math.PI, (float)Math.PI / 4 + (float)Math.PI / 180, home.getCamera());
    
    // 4. Remove 10� from camera yaw 
    tester.actionKeyStroke(KeyEvent.VK_LEFT);
    // Check camera location and angles
    assertCoordinatesAndAnglesEqualCameraLocationAndAngles(263.9243f, 994.1371f, 1017.3002f, 
        (float)Math.PI - 10 * (float)Math.PI / 180, (float)Math.PI / 4 + (float)Math.PI / 180, home.getCamera());
    // Add 1� to camera yaw 
    tester.actionKeyPress(KeyEvent.VK_SHIFT);
    tester.actionKeyStroke(KeyEvent.VK_RIGHT);
    tester.actionKeyRelease(KeyEvent.VK_SHIFT);
    // Check camera location and angles
    assertCoordinatesAndAnglesEqualCameraLocationAndAngles(247.0138f, 996.967f, 1017.3002f, 
        (float)Math.PI - 9 * (float)Math.PI / 180, (float)Math.PI / 4 + (float)Math.PI / 180, home.getCamera());
    
    // 5. Move camera 1cm forward
    tester.actionKeyPress(KeyEvent.VK_SHIFT);
    tester.actionKeyStroke(KeyEvent.VK_UP);
    tester.actionKeyRelease(KeyEvent.VK_SHIFT);
    // Check camera location and angles
    assertCoordinatesAndAnglesEqualCameraLocationAndAngles(246.4704f, 993.5365f, 1013.7034f, 
        (float)Math.PI - 9 * (float)Math.PI / 180, (float)Math.PI / 4 + (float)Math.PI / 180, home.getCamera());
    // Move camera 10 backward 
    tester.actionKeyStroke(KeyEvent.VK_DOWN);
    // Check camera location and angles
    assertCoordinatesAndAnglesEqualCameraLocationAndAngles(251.9039f, 1027.8417f, 1049.6704f, 
        (float)Math.PI - 9 * (float)Math.PI / 180, (float)Math.PI / 4 + (float)Math.PI / 180, home.getCamera());
    
    // 6. View from observer
    runAction(controller, HomePane.ActionType.VIEW_FROM_OBSERVER);
    tester.waitForIdle();
    ObserverCamera observerCamera = home.getObserverCamera();
    // Check camera is the observer camera
    assertSame("Camera isn't observer camera", observerCamera, home.getCamera());
    // Check default camera location and angles
    assertCoordinatesAndAnglesEqualCameraLocationAndAngles(100, 100, 170, 
        3 * (float)Math.PI / 4, (float)Math.PI / 16, home.getCamera());
    // Check observer camera is selected
    assertEquals("Wrong selected items count", 1, home.getSelectedItems().size());
    assertTrue("Camera isn't selected", home.getSelectedItems().contains(home.getCamera()));

    // Try to select wall and observer camera 
    runAction(controller, HomePane.ActionType.SELECT);
    tester.actionClick(planComponent, 50, 50);
    tester.actionKeyPress(KeyEvent.VK_SHIFT);
    tester.actionClick(planComponent, (int)(140 * planComponent.getScale()), 
        (int)(140 * planComponent.getScale()));
    tester.actionKeyRelease(KeyEvent.VK_SHIFT);
    // Check selected items contains only wall
    assertEquals("Wrong selected items count", 1, home.getSelectedItems().size());
    assertTrue("Wall isn't selected", home.getSelectedItems().contains(wall));
    
    // Select observer camera
    Thread.sleep(1000); // Wait 1s to avoid double click
    tester.actionClick(planComponent, (int)(140 * planComponent.getScale()), 
        (int)(140 * planComponent.getScale()));
    // Check observer camera is selected
    assertEquals("Wrong selected items count", 1, home.getSelectedItems().size());
    assertTrue("Camera isn't selected", home.getSelectedItems().contains(home.getCamera()));
    
    // 7. Move observer camera at right and down
    tester.actionKeyStroke(KeyEvent.VK_RIGHT);
    tester.actionKeyStroke(KeyEvent.VK_DOWN);
    // Check camera location and angles
    assertCoordinatesAndAnglesEqualCameraLocationAndAngles(100 + 1 / planComponent.getScale(), 
        100 + 1 / planComponent.getScale(), 170, 
        3 * (float)Math.PI / 4, (float)Math.PI / 16, home.getCamera());
    
    // 8. Change observer camera yaw by moving its yaw indicator
    float [][] cameraPoints = observerCamera.getPoints();
    int xYawIndicator = (int)(((40 + (cameraPoints[0][0] + cameraPoints[3][0]) / 2)) * planComponent.getScale());
    int yYawIndicator = (int)(((40 + (cameraPoints[0][1] + cameraPoints[3][1]) / 2)) * planComponent.getScale());
    tester.actionMousePress(planComponent, new ComponentLocation(
        new Point(xYawIndicator, yYawIndicator)));
    tester.actionMouseMove(planComponent, new ComponentLocation(
        new Point(xYawIndicator + 2, yYawIndicator + 2)));
    tester.actionMouseRelease();
    // Check camera yaw angle changed
    assertCoordinatesAndAnglesEqualCameraLocationAndAngles(100 + 1 / planComponent.getScale(), 
        100 + 1 / planComponent.getScale(), 170, 
        2.5156f, (float)Math.PI / 16, home.getCamera());

    // Change observer camera pitch by moving its pitch indicator
    cameraPoints = observerCamera.getPoints();
    int xPitchIndicator = (int)(((40 + (cameraPoints[1][0] + cameraPoints[2][0]) / 2)) * planComponent.getScale());
    int yPitchIndicator = (int)(((40 + (cameraPoints[1][1] + cameraPoints[2][1]) / 2)) * planComponent.getScale());
    tester.actionMousePress(planComponent, new ComponentLocation(
        new Point(xPitchIndicator, yPitchIndicator)));
    tester.actionMouseMove(planComponent, new ComponentLocation(
        new Point(xPitchIndicator + 2, yPitchIndicator + 2)));
    tester.actionMouseRelease();
    // Check camera pitch angle changed
    assertCoordinatesAndAnglesEqualCameraLocationAndAngles(100 + 1 / planComponent.getScale(), 
        100 + 1 / planComponent.getScale(), 170, 
        2.5156f, 0.1639f, home.getCamera());
    
    // 9. Change observer camera location with mouse in 3D view
    tester.actionMousePress(component3D, new ComponentLocation(new Point(10, 10)));
    tester.actionKeyPress(KeyEvent.VK_ALT);
    tester.actionMouseMove(component3D, new ComponentLocation(new Point(10, 20)));
    tester.actionKeyRelease(KeyEvent.VK_ALT);
    tester.actionMouseRelease();
    // Check camera location changed
    assertCoordinatesAndAnglesEqualCameraLocationAndAngles(130.6284f, 141.8525f, 170, 
        2.5156f, 0.1639f, home.getCamera());

    // 10. Change observer camera yaw with mouse in 3D view
    tester.actionMousePress(component3D, new ComponentLocation(new Point(10, 20)));
    tester.actionMouseMove(component3D, new ComponentLocation(new Point(20, 20)));
    tester.actionMouseRelease();
    // Check camera yaw changed
    assertCoordinatesAndAnglesEqualCameraLocationAndAngles(130.6284f, 141.8525f, 170, 
        3.2156f, 0.1639f, home.getCamera());
    
    // Change camera pitch with mouse in 3D view
    tester.actionMousePress(component3D, new ComponentLocation(new Point(20, 20)));
    tester.actionMouseMove(component3D, new ComponentLocation(new Point(20, 30)));
    tester.actionMouseRelease();
    // Check camera yaw changed
    assertCoordinatesAndAnglesEqualCameraLocationAndAngles(130.6284f, 141.8525f, 170, 
        3.2156f, 0.2339f, home.getCamera());
    
    // 11. Edit 3D view modal dialog box
    JDialog attributesDialog = showHome3DAttributesPanel(controller, frame, tester);
    // Retrieve Home3DAttributesPanel components
    Home3DAttributesPanel panel = (Home3DAttributesPanel)TestUtilities.findComponent(
        attributesDialog, Home3DAttributesPanel.class);
    Home3DAttributesController panelController = 
        (Home3DAttributesController)TestUtilities.getField(panel, "controller");
    JSpinner observerFieldOfViewSpinner = 
        (JSpinner)TestUtilities.getField(panel, "observerFieldOfViewSpinner");
    JSpinner observerHeightSpinner = 
        (JSpinner)TestUtilities.getField(panel, "observerHeightSpinner");
    ColorButton groundColorButton =  
        (ColorButton)TestUtilities.getField(panel, "groundColorButton");
    ColorButton skyColorButton = 
        (ColorButton)TestUtilities.getField(panel, "skyColorButton");
    JSlider brightnessSlider = 
        (JSlider)TestUtilities.getField(panel, "brightnessSlider");
    JSlider wallsTransparencySlider = 
        (JSlider)TestUtilities.getField(panel, "wallsTransparencySlider");
    // Check edited values
    float oldCameraFieldOfView = observerCamera.getFieldOfView();
    float oldCameraHeight = observerCamera.getHeight();
    int oldGroundColor = home.getGroundColor();
    TextureImage oldGroundTexture = home.getGroundTexture();
    int oldSkyColor = home.getSkyColor();
    int oldLightColor = home.getLightColor();
    float oldWallsAlpha = home.getWallsAlpha();
    assertEquals("Wrong field of view", (int)Math.round(Math.toDegrees(oldCameraFieldOfView)), 
        observerFieldOfViewSpinner.getValue());
    assertEquals("Wrong height", (float)Math.round(oldCameraHeight * 100) / 100, 
        observerHeightSpinner.getValue());
    assertEquals("Wrong ground color", oldGroundColor, 
        groundColorButton.getColor().intValue());
    assertEquals("Wrong ground texture", oldGroundTexture, 
        panelController.getGroundTextureController().getTexture());
    assertEquals("Wrong sky color", oldSkyColor, 
        skyColorButton.getColor().intValue());
    assertEquals("Wrong brightness", oldLightColor & 0xFF, 
        brightnessSlider.getValue());
    assertEquals("Wrong transparency", (int)(oldWallsAlpha * 255), 
        wallsTransparencySlider.getValue());
    
    // 12. Change dialog box values
    observerFieldOfViewSpinner.setValue(90);
    observerHeightSpinner.setValue(300f);
    groundColorButton.setColor(0xFFFFFF);
    skyColorButton.setColor(0x000000);
    brightnessSlider.setValue(128);
    wallsTransparencySlider.setValue(128);
    // Click on Ok in dialog box
    doClickOnOkInDialog(attributesDialog, tester);
    // Check home attributes are modified accordingly
    assert3DAttributesEqualHomeAttributes((float)Math.toRadians(90), 300f, 
        0xFFFFFF, null, 0x000000, 0x808080, 1 / 255f * 128f, home);
    
    // 13. Undo changes
    runAction(controller, HomePane.ActionType.UNDO);
    // Check home attributes have previous values
    assert3DAttributesEqualHomeAttributes(oldCameraFieldOfView, oldCameraHeight, 
        oldGroundColor, null, oldSkyColor, oldLightColor, oldWallsAlpha, home);
    // Redo
    runAction(controller, HomePane.ActionType.REDO);
    // Check home attributes are modified accordingly
    assert3DAttributesEqualHomeAttributes((float)Math.toRadians(90), 300f, 
        0xFFFFFF, null, 0x000000, 0x808080, 1 / 255f * 128f, home);
    
    // 14. Edit 3D view modal dialog box to change ground texture
    attributesDialog = showHome3DAttributesPanel(controller, frame, tester);
    panel = (Home3DAttributesPanel)TestUtilities.findComponent(
        attributesDialog, Home3DAttributesPanel.class);
    panelController = (Home3DAttributesController)TestUtilities.getField(panel, "controller");
    JRadioButton groundColorRadioButton = 
        (JRadioButton)TestUtilities.getField(panel, "groundColorRadioButton");
    final TextureChoiceComponent groundTextureButton = 
        (TextureChoiceComponent)panelController.getGroundTextureController().getView();
    JRadioButton groundTextureRadioButton = 
        (JRadioButton)TestUtilities.getField(panel, "groundTextureRadioButton");
    
    // Check color and texture radio buttons
    assertTrue("Ground color radio button isn't checked", 
        groundColorRadioButton.isSelected());
    assertFalse("Ground texture radio button is checked", 
        groundTextureRadioButton.isSelected());
    // Click on ground texture button
    tester.invokeLater(new Runnable() { 
        public void run() {
          // Display texture dialog later in Event Dispatch Thread to avoid blocking test thread
          groundTextureButton.doClick();
        }
      });
    // Wait for 3D view to be shown
    String groundTextureTitle = ResourceBundle.getBundle(
        Home3DAttributesController.class.getName()).getString("groundTextureTitle");
    tester.waitForFrameShowing(new AWTHierarchy(), groundTextureTitle);
    // Check texture dialog box is displayed
    JDialog textureDialog = (JDialog)new BasicFinder().find(attributesDialog, 
        new WindowMatcher(groundTextureTitle));
    assertTrue("Texture dialog not showing", textureDialog.isShowing());
    
    JList availableTexturesList = (JList)new BasicFinder().find(textureDialog, 
        new ClassMatcher(JList.class, true));
    availableTexturesList.setSelectedIndex(0);
    CatalogTexture firstTexture = preferences.getTexturesCatalog().getCategories().get(0).getTexture(0);
    assertEquals("Wrong first texture in list", firstTexture, 
        availableTexturesList.getSelectedValue());
    
    // Click on OK in texture dialog box
    doClickOnOkInDialog(textureDialog, tester);
    // Check color and texture radio buttons
    assertFalse("Ground color radio button is checked", 
        groundColorRadioButton.isSelected());
    assertTrue("Ground texture radio button isn't checked", 
        groundTextureRadioButton.isSelected());
    
    // Click on OK in 3D attributes dialog box
    doClickOnOkInDialog(attributesDialog, tester);
    
    // Check home attributes are modified accordingly
    assert3DAttributesEqualHomeAttributes((float)Math.toRadians(90), 300f, 
        0xFFFFFF, firstTexture, 0x000000, 0x808080, 1 / 255f * 128f, home);
  }

  /**
   * Returns the dialog that displays home 3D attributes. 
   */
  private JDialog showHome3DAttributesPanel(final HomeController controller, 
                                            JFrame parent, JComponentTester tester) 
            throws ComponentSearchException {
    tester.invokeLater(new Runnable() { 
        public void run() {
          // Display dialog box later in Event Dispatch Thread to avoid blocking test thread
          runAction(controller, HomePane.ActionType.MODIFY_3D_ATTRIBUTES);
        }
      });
    // Wait for 3D view to be shown
    tester.waitForFrameShowing(new AWTHierarchy(), ResourceBundle.getBundle(
        Home3DAttributesPanel.class.getName()).getString("home3DAttributes.title"));
    // Check dialog box is displayed
    JDialog attributesDialog = (JDialog)new BasicFinder().find(parent, 
        new ClassMatcher (JDialog.class, true));
    assertTrue("3D view dialog not showing", attributesDialog.isShowing());
    return attributesDialog;
  }
  
  /**
   * Cliks on OK in dialog to close it.
   */
  private void doClickOnOkInDialog(JDialog dialog, JComponentTester tester) 
            throws ComponentSearchException {
    final JOptionPane attributesOptionPane = (JOptionPane)TestUtilities.findComponent(
        dialog, JOptionPane.class);
    tester.invokeAndWait(new Runnable() {
        public void run() {
          // Select Ok option to hide dialog box in Event Dispatch Thread
          attributesOptionPane.setValue(JOptionPane.OK_OPTION); 
        }
      });
    assertFalse("Dialog still showing", dialog.isShowing());
  }

  /**
   * Runs <code>actionPerformed</code> method matching <code>actionType</code> 
   * in <code>HomePane</code>. 
   */
  private void runAction(HomeController controller,
                         HomePane.ActionType actionType) {
    ((JComponent)controller.getView()).getActionMap().get(actionType).actionPerformed(null);
  }

  /**
   * Asserts the location and angles of <code>camera</code> are the point 
   * at (<code>x</code>, <code>y</code>, <code>z</code>) and the angles
   * <code>yaw</code> and <code>pitch</code>. 
   */
  private void assertCoordinatesAndAnglesEqualCameraLocationAndAngles(float x, float y, 
                                                float z, float yaw, float pitch, 
                                                Camera camera) {
    assertTrue("Incorrect X " + camera.getX() + ", should be " + x, 
        Math.abs(x - camera.getX()) < 1E-3);
    assertTrue("Incorrect Y " + camera.getY() + ", should be " + y, 
        Math.abs(y - camera.getY()) < 1E-3);
    assertTrue("Incorrect Z " + camera.getZ() + ", should be " + z, 
        Math.abs(z - camera.getZ()) < 1E-3);
    assertTrue("Incorrect yaw " + camera.getYaw() + ", should be " + yaw, 
        Math.abs(yaw - camera.getYaw()) < 1E-3);
    assertTrue("Incorrect pitch " + camera.getPitch() + ", should be " + pitch, 
        Math.abs(pitch - camera.getPitch()) < 1E-3);
  }
  
  /**
   * Asserts the 3D attributes given in parameter match <code>home</code> 3D attributes.
   */
  private void assert3DAttributesEqualHomeAttributes(float cameraFieldOfView, 
                                                     float cameraHeight, 
                                                     int groundColor, 
                                                     TextureImage groundTexture,
                                                     int skyColor, 
                                                     int lightColor, 
                                                     float wallsAlpha, Home home) {
    ObserverCamera observerCamera = home.getObserverCamera();
    assertEquals("Wrong field of view", cameraFieldOfView, observerCamera.getFieldOfView());
    assertEquals("Wrong height", cameraHeight, observerCamera.getHeight());
    assertEquals("Wrong ground color", groundColor, home.getGroundColor());
    if (groundTexture == null) {
      assertEquals("Wrong ground texture", groundTexture, home.getGroundTexture());
    } else {
      assertEquals("Wrong ground texture", groundTexture.getName(), home.getGroundTexture().getName());
    }
    assertEquals("Wrong sky color", skyColor, home.getSkyColor());
    assertEquals("Wrong brightness", lightColor, home.getLightColor());
    assertEquals("Wrong transparency", wallsAlpha, home.getWallsAlpha());
  }
}
