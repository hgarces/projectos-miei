import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.media.opengl.GL2;

/**
 * @author Andre Correia, 41626
 * @author Henrique Garces, 42077
 */

public class ObjectLoader {

	private final static String OBJ_VERTEX = "v";
	private final static String OBJ_VERTEX_TEXTURE = "vt";
	private final static String OBJ_FACE = "f";
	private final static float MAX_SIZE = 1.0f;

	private ArrayList<float[]> vertexes;	//coordenadas dos vertices
	private ArrayList<float[]> textures;	//coordenadas dos vertices da textura
	private ArrayList<int[]> vertIndices;	//indice das coord dos vertices
	private ArrayList<int[]> textIndices;	//indice das coord dos vertices da textura

	private boolean isFirstVertex = true;
	public float leftPoint = 0f;
	public float rightPoint = 0f;
	public float topPoint = 0f;
	public float bottomPoint = 0f;
	public float nearPoint = 0f;
	public float farPoint = 0f;

	public ObjectLoader() {
		vertexes = new ArrayList<float[]>();
		textures = new ArrayList<float[]>();
		vertIndices = new ArrayList<int[]>();
		textIndices = new ArrayList<int[]>();
	}
	
	public void draw(GL2 gl) {
		for(int i = 0; i < vertIndices.size(); i++) {
			int[] tempfaces = vertIndices.get(i);
			int[] tempfacetext = textIndices.get(i);
			
			gl.glBegin(GL2.GL_POLYGON);
			for(int j = 0; j < tempfaces.length; j++) {
				if(tempfacetext[j] != 0) {
					float xtex = textures.get(tempfacetext[j]-1)[0];
					float ytex = textures.get(tempfacetext[j]-1)[1];
					gl.glTexCoord2f(xtex, ytex);
				}
				float x = vertexes.get(tempfaces[j]-1)[0];
				float y = vertexes.get(tempfaces[j]-1)[1];
				float z = vertexes.get(tempfaces[j]-1)[2];
				gl.glVertex3f(x, y, z);
			}
			gl.glEnd();
		}
		
	}

	public void load(File objFile) throws IOException {

		FileReader fileReader = new FileReader(objFile);
		BufferedReader bufferedReader = new BufferedReader(fileReader);

		int lineCount = 0;

		String line = null;
		while(true) {
			line = bufferedReader.readLine();
			if(null == line) {
				break;
			}

			line = line.trim();

			if(line.length() == 0)
				continue;

			String tokens[] = line.split("[\t ]+");

			if(line.startsWith("#")) {
				continue;
			}
			else if(tokens[0].equals(OBJ_VERTEX)) {
				float[] v = new float[3];
				v[0] = Float.parseFloat(tokens[1]);
				v[1] = Float.parseFloat(tokens[2]);
				v[2] = Float.parseFloat(tokens[3]);
				vertexes.add(v);
				if(isFirstVertex) {
					rightPoint = v[0];
					leftPoint = v[0];
					topPoint = v[1];
					bottomPoint = v[1];
					nearPoint = v[2];
					farPoint = v[2];
					isFirstVertex = false;
				}
				else
					updateCoordinates(v);
			} 
			else if(tokens[0].equals(OBJ_VERTEX_TEXTURE)) {
				float[] vt = new float[2];
				vt[0] = Float.parseFloat(tokens[1]);
				vt[1] = Float.parseFloat(tokens[2]);
				textures.add(vt);
			}
			else if(tokens[0].equals(OBJ_FACE)) {
				int size = tokens.length-1;
				int[] vIndex = new int[size];
				int[] tIndex = new int[size];

				for(int i = 0; i < size; i++) {
					String[] temp = tokens[i+1].split("/");
					int n = temp.length;
					if(n == 3) n = 2;
					vIndex[i] = Integer.parseInt(temp[0]);
					if(n > 1) {
						if(!temp[1].equals(""))
							tIndex[i] = Integer.parseInt(temp[1]);
					}
					if(n > 2){ }	//vn, nao se faz nada
				}
				vertIndices.add(vIndex);
				textIndices.add(tIndex);
			}

			lineCount++;
		}
		bufferedReader.close();
		System.err.println("Loaded " + lineCount + " lines");
	}

	public float getScaleFactor() {
		float dx = rightPoint - leftPoint;
		float dy = topPoint - bottomPoint;
		float dz = nearPoint - farPoint;
		
		return (MAX_SIZE/Math.max(Math.max(dx, dy), dz));
	}

	public float[] getCenter() {
		float[] center = new float[3];
		center[0] = (rightPoint + leftPoint)/2.0f;
		center[1] = (topPoint + bottomPoint)/2.0f;
		center[2] = (nearPoint + farPoint)/2.0f;
		return center;
		
	}
	private void updateCoordinates(float[] v) {
		if(v[0] > rightPoint)
			rightPoint = v[0];
		if(v[0] < leftPoint)
			leftPoint = v[0];
		if(v[1] > topPoint)
			topPoint = v[1];
		if(v[1] < bottomPoint)
			bottomPoint = v[1];
		if(v[2] > nearPoint)
			nearPoint = v[2];
		if(v[2] < farPoint)
			farPoint = v[2];

	}

}


