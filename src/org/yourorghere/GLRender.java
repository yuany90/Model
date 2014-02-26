/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.yourorghere;

import com.obj.Group;
import com.obj.Vertex;
import com.obj.Face;
import com.obj.Material;
import com.obj.*;
import com.sun.opengl.util.texture.Texture;
import com.obj.TextureCoordinate;
import com.sun.opengl.util.GLUT;
import com.sun.opengl.util.texture.TextureData;
import javax.media.opengl.GL;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import java.io.*;
import javax.media.opengl.glu.GLUquadric;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import com.sun.opengl.util.texture.*;
/**
 *
 * @author yuanlu
 */
public class GLRender extends MouseAdapter implements KeyListener, GLEventListener{
    private String cubeMapFileName[] =
    {  "skybox/right.jpg", 
       "skybox/left.jpg",
       "skybox/top.jpg",
       "skybox/bottom.jpg",
       "skybox/front.jpg",
       "skybox/back.jpg",
     };
    private int textures[];
    private Texture cube;
    private com.obj.Texture t[];
    private GL gl;
    private GLU glu = new GLU();
    private float cameraPos[] = {0.0f, 1.0f, 0.0f};
    private float targetPos[] = {0.0f, 1.0f, 20.0f};
    private float alpha = 0.0f;
    private float belta = 0.0f;
    private float woverh;
    private WavefrontObject church;
    public void rotation(){
        targetPos[0] = cameraPos[0] + 20.0f * (float)Math.cos(alpha) * (float)Math.cos(belta);
        targetPos[1] = cameraPos[1] + 20.0f * (float)Math.sin(alpha);
        targetPos[2] = cameraPos[2] + 20.0f * (float)Math.cos(alpha) * (float)Math.sin(belta);
    };

    Hashtable<com.obj.Texture, Integer> textureLocation;
    public void init(GLAutoDrawable drawable) {
        gl = drawable.getGL();
        try{
            loadAllTexture();
            loadAllShader();
        } catch(IOException e){
            System.err.println("Texture load failed.");
            System.exit(0);
        }
        sky = gl.glCreateProgram();
        gl.glAttachShader(sky, skyShaderV);
        gl.glAttachShader(sky, skyShaderF);
        gl.glLinkProgram(sky);
        gl.glValidateProgram(sky);
        cubeSky = gl.glGetUniformLocationARB(sky, "cubeMap");
        church = new WavefrontObject("3dModel/house_obj.obj");
        int matSize = church.getMaterials().size();
        t = new com.obj.Texture[matSize];
        textures = new int[matSize];
        textureLocation = new Hashtable<com.obj.Texture, Integer>();
        gl.glGenTextures(matSize, textures, 0);
        System.out.println(church.getMaterials());
        Enumeration<Material> matEnum = church.getMaterials().elements();
        for(int i = 0; i< matSize; i++){
            System.out.println("i is " + i);
            Material m = matEnum.nextElement();
            System.out.println(m);
            t[i] = m.getTexture();
            
            if(t[i] == null) continue;
            System.out.println(t[i]);
            textureLocation.put(t[i], i);
            gl.glBindTexture(GL.GL_TEXTURE_2D, textures[i]);
            gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, t[i].getWidth(), t[i].getHeight(), 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, t[i].getPixels());
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
        }

        gl.setSwapInterval(1);
        // Setup the drawing area and shading mode
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glClearDepth(1.0f);
        gl.glShadeModel(GL.GL_SMOOTH); // try setting this to GL_FLAT and see what happens.
        gl.glEnable(GL.GL_TEXTURE);
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glEnable(GL.GL_LIGHTING);
        gl.glEnable(GL.GL_LIGHT1);
        gl.glDepthFunc(GL.GL_LEQUAL);
        gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
    }
 
    private void drawObj(WavefrontObject m) {
        if (m != null) {
            com.obj.Texture currentText;
            gl.glPushMatrix();
            gl.glTranslatef(0.4f, 0.6f ,0.0f);
            gl.glScalef(0.002f, 0.002f, 0.002f);
            gl.glColor3f(1.0f,1.0f,1.0f);
            ArrayList<Group> groups = m.getGroups();
            for (int i = 0; i < groups.size(); i++) {
                currentText = m.getGroups().get(i).getMaterial().getTexture();
                if(currentText!=null){
                    int index = textureLocation.get(currentText);
                    gl.glBindTexture(GL.GL_TEXTURE_2D, textures[index]);
                }
                ArrayList<Face> faces = groups.get(i).getFaces();
                int counter = 0;
                while (counter < faces.size()) {
                    Face face = faces.get(counter);
                    int glType = face.getType() == Face.GL_TRIANGLES ? GL.GL_TRIANGLES : GL.GL_QUADS;
                    gl.glBegin(glType);
                    //vertices1
                    int loop = face.getType() == Face.GL_TRIANGLES ? 2 : 3;
                    while(loop >= 0){
                        if(face.getNormals() != null){
                            Vertex n = face.getNormals()[loop];
                            gl.glNormal3f(n.getX(), n.getY(), n.getZ());
                        }
                        if(face.getTextures() != null){
                            TextureCoordinate t = face.getTextures()[loop];
                            gl.glTexCoord2f(t.getU(), t.getV());
                        }
                        if(face.getVertices() != null){
                            Vertex v = face.getVertices()[loop];
                            if(v != null){
                            gl.glVertex3f(v.getX(), v.getY(), v.getZ());
                            }
                        }
                        loop--;
                    }
                    gl.glEnd();
                    counter++;
                }
            }
        }

    }
    int sky, cubeSky;
    public void drawSkyBox(){
         //Forward
        gl.glDisable(GL.GL_DEPTH_TEST);
        gl.glUseProgram(sky);
        gl.glUniform1i(cubeSky, 0);
        cube.bind();
        gl.glPushMatrix();
        gl.glTranslated(cameraPos[0], cameraPos[1], cameraPos[2]);
        gl.glBegin(GL.GL_QUADS);
        // Front Face
        gl.glNormal3d(0, 0, 1);
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(-1.0f, 1.0f, 1.0f);
        gl.glTexCoord2f(-1.0f, 0.0f);
        gl.glVertex3f(1.0f, 1.0f, 1.0f);
        gl.glTexCoord2f(-1.0f, 1.0f);
        gl.glVertex3f(1.0f, -1.0f, 1.0f);
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(-1.0f, -1.0f, 1.0f);
        gl.glEnd();

        gl.glBegin(GL.GL_QUADS);
        // Back Face
        gl.glNormal3d(0, 0, -1);
        gl.glTexCoord2f(-1.0f, 0.0f);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f);
        gl.glTexCoord2f(-1.0f, 1.0f);
        gl.glVertex3f(-1.0f, -1.0f, -1.0f);
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(1.0f, -1.0f, -1.0f);
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(1.0f, 1.0f, -1.0f);
        gl.glEnd();

        gl.glBegin(GL.GL_QUADS);
        // Top Face
        gl.glNormal3d(0, 1, 0);
        gl.glTexCoord2f(0.0f, -1.0f);
        gl.glVertex3f(-1.0f, -1.0f, -1.0f);
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(-1.0f, -1.0f, 1.0f);
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3f(1.0f, -1.0f, 1.0f);
        gl.glTexCoord2f(1.0f, -1.0f);
        gl.glVertex3f(1.0f, -1.0f, -1.0f);
        gl.glEnd();

        gl.glBegin(GL.GL_QUADS);
        // Bottom Face
        gl.glNormal3d(0, -1, 0);
        gl.glTexCoord2f(-1.0f, 1.0f);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f);
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(1.0f, 1.0f, -1.0f);
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(1.0f, 1.0f, 1.0f);
        gl.glTexCoord2f(-1.0f, 0.0f);
        gl.glVertex3f(-1.0f, 1.0f, 1.0f);
        gl.glEnd();

        gl.glBegin(GL.GL_QUADS);
        // Right face
        gl.glNormal3d(1, 0, 0);
        gl.glTexCoord2f(-1.0f, 0.0f);
        gl.glVertex3f(1.0f, 1.0f, -1.0f);
        gl.glTexCoord2f(-1.0f, 1.0f);
        gl.glVertex3f(1.0f, -1.0f, -1.0f);
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(1.0f, -1.0f, 1.0f);
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(1.0f, 1.0f, 1.0f);
        gl.glEnd();

        gl.glBegin(GL.GL_QUADS);
        // Left Face
        gl.glNormal3d(-1, 0, 0);
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f);
        gl.glTexCoord2f(-1.0f, 0.0f);
        gl.glVertex3f(-1.0f, 1.0f, 1.0f);
        gl.glTexCoord2f(-1.0f, 1.0f);
        gl.glVertex3f(-1.0f, -1.0f, 1.0f);
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(-1.0f, -1.0f, -1.0f);
        gl.glEnd();
        gl.glPopMatrix();
        gl.glUseProgram(0);
        gl.glEnable(GL.GL_DEPTH_TEST);
    }
    
    public void display(GLAutoDrawable drawable) {
        // Clear the drawing area
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glColor3f(1.0f, 1.0f, 1.0f);
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(60.0f, woverh, 0.01, 100.0);
        rotation();
        glu.gluLookAt(cameraPos[0], cameraPos[1], cameraPos[2],
            targetPos[0], targetPos[1], targetPos[2], 0, 1, 0);
        // Reset the current matrix to the "identity"
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
        float LightAmbient[]= { 1.0f, 1.0f, 1.0f, 1.0f }; 
        float LightDiffuse[]= { 1.0f, 1.0f, 1.0f, 1.0f };
        float LightPosition[]= { cameraPos[0], cameraPos[1], cameraPos[2], 1.0f }; 
        gl.glLightfv(GL.GL_LIGHT1, GL.GL_AMBIENT, LightAmbient,0); 
        gl.glLightfv(GL.GL_LIGHT1, GL.GL_DIFFUSE, LightDiffuse,0); 
        gl.glLightfv(GL.GL_LIGHT1, GL.GL_POSITION,LightPosition,0);  
        drawSkyBox();
        drawObj(church);
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL gl = drawable.getGL();
        GLU glu = new GLU();
        if (height <= 0) { // avoid a divide by zero error!
            height = 1;
        }
        woverh = (float) width / (float) height;
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(60.0f, woverh, 0.01, 100.0);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
    }
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
    }

    public Texture loadCubeTextFromFile(String[] fileName) throws IOException{
        Texture cube = TextureIO.newTexture(GL.GL_TEXTURE_CUBE_MAP);
        TextureData posx = TextureIO.newTextureData(new File(fileName[0]),GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, false, TextureIO.JPG);
        TextureData negx = TextureIO.newTextureData(new File(fileName[1]),GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, false, TextureIO.JPG);
        TextureData posy = TextureIO.newTextureData(new File(fileName[2]),GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, false,  TextureIO.JPG);
        TextureData negy = TextureIO.newTextureData(new File(fileName[3]),GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, false,  TextureIO.JPG);
        TextureData posz = TextureIO.newTextureData(new File(fileName[4]), GL.GL_RGBA, GL.GL_UNSIGNED_BYTE,false,  TextureIO.JPG);
        TextureData negz = TextureIO.newTextureData(new File(fileName[5]),GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, false, TextureIO.JPG);
        
        cube.updateImage(posx, GL.GL_TEXTURE_CUBE_MAP_POSITIVE_X);
        cube.updateImage(negx, GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_X);
        cube.updateImage(posy, GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Y);
        cube.updateImage(negy, GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y);
        cube.updateImage(posz, GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Z);
        cube.updateImage(negz, GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z);
        return cube;
    } 

    public Texture loadTextureFromFile (String fileName) throws IOException{
        File file = new File(fileName);
        try{
            Texture t = TextureIO.newTexture(file, true);
            return t;
        } catch(IOException e){
            throw e;
        }
    }
//    TextureData top, left, right, bottom, back, front;	         
    public void loadAllTexture() throws IOException{

            cube = loadCubeTextFromFile(cubeMapFileName);
            gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
            gl.glTexParameteri(GL.GL_TEXTURE_CUBE_MAP, GL.GL_TEXTURE_WRAP_S,
                    GL.GL_REPEAT);
            gl.glTexParameteri(GL.GL_TEXTURE_CUBE_MAP, GL.GL_TEXTURE_WRAP_T,
                    GL.GL_REPEAT);
            gl.glTexParameteri(GL.GL_TEXTURE_CUBE_MAP, GL.GL_TEXTURE_WRAP_R,
                    GL.GL_REPEAT);
            gl.glTexParameteri(GL.GL_TEXTURE_CUBE_MAP, GL.GL_TEXTURE_MAG_FILTER,
                    GL.GL_LINEAR);
            gl.glTexParameteri(GL.GL_TEXTURE_CUBE_MAP, GL.GL_TEXTURE_MIN_FILTER,
                    GL.GL_LINEAR);
            gl.glTexGeni(GL.GL_S, GL.GL_TEXTURE_GEN_MODE, GL.GL_NORMAL_MAP);
            gl.glTexGeni(GL.GL_T, GL.GL_TEXTURE_GEN_MODE, GL.GL_NORMAL_MAP);
            gl.glTexGeni(GL.GL_R, GL.GL_TEXTURE_GEN_MODE, GL.GL_NORMAL_MAP);
            gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);

    }
    int skyShaderV, skyShaderF;
    public void loadAllShader()throws IOException{
            skyShaderV = gl.glCreateShader(GL.GL_VERTEX_SHADER_ARB);
            skyShaderF = gl.glCreateShader(GL.GL_FRAGMENT_SHADER_ARB);
            loadShaderFromFile(skyShaderV, "shader/sky.vert");
            loadShaderFromFile(skyShaderF, "shader/sky.frag");
    }
    private void loadShaderFromFile (int shader, String fileName) throws IOException {
        try {
            BufferedReader brv = new BufferedReader(new FileReader(fileName));
            String vsrc = "";
            String line;
            while ((line = brv.readLine()) != null) {
                vsrc += line + "\n";
            }
            gl.glShaderSource(shader, 1, new String[]{vsrc}, (int[]) null, 0);
        } catch (IOException e) {
            throw e;
        }
        gl.glCompileShader(shader);
    }
    public synchronized void keyPressed(KeyEvent e){
	if(e.getKeyCode() == KeyEvent.VK_UP){
     //       System.out.println("up key pressed " + alpha);
            if(alpha + 0.1f < Math.PI/2)
            alpha += 0.1f;
	}
	if(e.getKeyCode() == KeyEvent.VK_DOWN){
            if(alpha - 0.1f > -Math.PI/2)
            alpha -= 0.1f;
	}
	if(e.getKeyCode() == KeyEvent.VK_RIGHT){
            belta += 0.1f;
	}
	if(e.getKeyCode() == KeyEvent.VK_LEFT){
            belta -=0.1f;
	}
	if(e.getKeyCode() == KeyEvent.VK_A){ //left
            cameraPos[0] += 0.01f * Math.sin(belta);
            cameraPos[2] -= 0.01f * Math.cos(belta);
        }
	if(e.getKeyCode() == KeyEvent.VK_D){ //right
            cameraPos[0] -= 0.01f * Math.sin(belta);
            cameraPos[2] += 0.01f * Math.cos(belta);
	}
	if(e.getKeyCode() == KeyEvent.VK_W){ //front
            cameraPos[0] += 0.01f * Math.cos(belta);
            cameraPos[2] += 0.01f * Math.sin(belta);
	}
	if(e.getKeyCode() == KeyEvent.VK_S){ //back
            cameraPos[0] -= 0.01f * Math.cos(belta);
            cameraPos[2] -= 0.01f * Math.sin(belta);
	}
	if(e.getKeyCode() == KeyEvent.VK_PAGE_UP){
            cameraPos[1] += 0.1f;
	}
	if(e.getKeyCode() ==  KeyEvent.VK_PAGE_DOWN){
            cameraPos[1] -= 0.1f;
	}	

	}

    public synchronized void keyReleased(KeyEvent e){
        if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {

        }
    }
    public void keyTyped(KeyEvent e){
    }
}
