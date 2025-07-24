# Clasificación de Imágenes Dermatológicas con EfficientNetB0

Este proyecto utiliza Deep Learning para la clasificación automática de imágenes dermatológicas en diferentes categorías, haciendo especial énfasis en el manejo del desbalance de clases y la penalización por peligrosidad clínica.

---

# Índice
- [Modelo de Clasificación (EfficientNetB0)](#modelo-de-clasificación-efficientnetb0)
  - [Estructura del proyecto (modelo)](#estructura-del-proyecto-modelo)
  - [Flujo de trabajo del notebook principal](#flujo-de-trabajo-del-notebook-principal)
  - [Requisitos](#requisitos)
  - [Estructura esperada de los datos](#estructura-esperada-de-los-datos)
  - [Ejecución](#ejecución)
  - [Notas](#notas)
- [DermaAPP (Aplicación móvil)](#dermaapp-aplicación-móvil)
  - [Descripción](#descripción)
  - [Características principales](#características-principales)
  - [Tecnologías utilizadas](#tecnologías-utilizadas)
  - [Estructura del proyecto (app)](#estructura-del-proyecto-app)
  - [Instalación y ejecución de la app](#instalación-y-ejecución-de-la-app)
  - [Uso básico](#uso-básico)
- [Autores](#autores)

---

# Modelo de Clasificación (EfficientNetB0)

## Estructura del proyecto (modelo)

- `modelo/efficientnetb0_derma_v4.ipynb`: Notebook principal con todo el flujo de trabajo, desde la carga de datos hasta la evaluación del modelo.
- `tested_models/`: Carpeta con otros notebooks, imágenes y posibles modelos entrenados.
- `keras/`: Carpeta donde se guardan los modelos entrenados en formato Keras (`.keras`).
- `dataset/`: (No incluido aquí) Debe contener las imágenes organizadas en carpetas por clase, separadas en `train` y `test`.

## Flujo de trabajo del notebook principal

1. **Importación de librerías**  
   Se importan todas las librerías necesarias para el procesamiento, modelado y visualización.
2. **Configuración de parámetros y recursos**  
   Se definen los parámetros globales y se verifica la disponibilidad de GPU.
3. **Carga y preparación de datos**  
   Se cargan las imágenes desde las carpetas, se dividen en entrenamiento y validación, y se muestra información básica.
4. **Ajuste de pesos por clase y penalización por peligrosidad**  
   Se calculan los pesos para cada clase considerando el desbalance y se penalizan más las clases peligrosas (por ejemplo, lesiones malignas).
5. **Optimización del pipeline de datos**  
   Se aplican técnicas de cacheo y prefetch para acelerar el entrenamiento.
6. **Construcción y compilación del modelo**  
   Se utiliza EfficientNetB0 como base, añadiendo capas densas y técnicas de data augmentation.
7. **Entrenamiento del modelo**  
   Se entrena el modelo con los pesos ajustados y callbacks para evitar el sobreajuste.
8. **Visualización del entrenamiento**  
   Se muestran las curvas de pérdida y precisión, los pesos por clase y ejemplos de imágenes aumentadas.
9. **Evaluación y visualización de resultados**  
   Se evalúa el modelo en el conjunto de test, mostrando el reporte de clasificación y la matriz de confusión.

## Requisitos

- Python 3.8 o 3.9 (recomendado para compatibilidad con TensorFlow)
- TensorFlow >= 2.8 y <= 2.10
- Keras
- scikit-learn
- matplotlib
- seaborn
- pandas
- (Opcional) GPU compatible con CUDA

Instala los requisitos con:

```bash
pip install tensorflow keras scikit-learn matplotlib seaborn pandas
```

## Estructura esperada de los datos

```
dataset/
  multi-6/
    train/
      Clase1/
      Clase2/
      ...
    test/
      Clase1/
      Clase2/
      ...
```

Cada carpeta de clase debe contener las imágenes correspondientes.

## Ejecución

1. Descarga o clona este repositorio.
2. Coloca el dataset en la ruta esperada (`../dataset/multi-6/` respecto al notebook).
3. Abre y ejecuta el notebook `modelo/efficientnetb0_derma_v4.ipynb` en Jupyter Notebook o JupyterLab.
4. Sigue el flujo de celdas para entrenar y evaluar el modelo.

## Notas

- El notebook está estructurado con celdas de texto explicativas para facilitar la comprensión.
- Se recomienda usar un entorno virtual para evitar conflictos de dependencias.
- El ajuste de pesos por clase y peligrosidad es configurable según el problema clínico.

---

# DermaAPP (Aplicación móvil)

## Descripción

**DermaAPP** es una aplicación móvil nativa desarrollada en **Kotlin** para Android, que permite a los usuarios capturar o seleccionar imágenes de lesiones cutáneas y obtener una predicción automática de la categoría dermatológica, utilizando el modelo EfficientNetB0 entrenado en este repositorio.

## Características principales

- Interfaz moderna y fluida basada en **Jetpack Compose**.
- Captura de imágenes desde la cámara o selección desde la galería.
- Envío de imágenes a un backend para obtener la predicción de la IA.
- Visualización clara del resultado y del historial de análisis.
- Enfoque en la privacidad y seguridad de los datos del usuario.

## Tecnologías utilizadas

- **Kotlin** y **Jetpack Compose** para la interfaz de usuario.
- Arquitectura de proyecto Android estándar (`app/`, `res/`, `java/`).
- Librerías modernas como **Coil** para carga de imágenes.
- Comunicación con backend mediante HTTP.
- Gradle para la gestión de dependencias y build.

## Estructura del proyecto (app)

```
dermaapp/
  ├── app/
  │   ├── src/
  │   │   ├── main/
  │   │   │   ├── java/com/example/dermaApp/        # Código fuente principal (Kotlin)
  │   │   │   ├── res/                              # Recursos gráficos, layouts, strings, temas
  │   │   │   └── AndroidManifest.xml
  │   │   ├── test/                                 # Tests unitarios
  │   │   └── androidTest/                          # Tests instrumentados
  │   ├── build.gradle.kts
  │   └── proguard-rules.pro
  ├── build.gradle.kts
  ├── settings.gradle.kts
  └── gradle/                                       # Configuración de Gradle
```

## Instalación y ejecución de la app

1. Instala [Android Studio](https://developer.android.com/studio).
2. Clona este repositorio y abre la carpeta `dermaapp/` en Android Studio.
3. Sincroniza el proyecto para descargar las dependencias.
4. Conecta un dispositivo físico o inicia un emulador.
5. Haz clic en **Run** (`▶️`) para compilar y ejecutar la app.

## Uso básico

- Abre la app en tu dispositivo Android.
- Selecciona o toma una foto de la lesión cutánea.
- Pulsa en "Analizar" para obtener la predicción automática.
- Consulta el historial de análisis si lo deseas.

**Nota:** La app se comunica con el backend que sirve el modelo de IA. Asegúrate de tener el backend desplegado y accesible desde la app (puedes usar tu propio servidor local o desplegarlo en la nube).

---

## Autores

**👤 Álvaro Ordoño Saiz**  
*AI Research Engineer*  
Desarrollo y entrenamiento de modelo  
Graduado en Ing. Informática (Computadores).  
Estudiante de Máster en Inteligencia Artificial.  
✉️ [alvordsai@gmail.com](mailto:alvordsai@gmail.com) | [LinkedIn](https://www.linkedin.com/in/álvaro-ordoño-saiz-4a0b982a7)

---

**👤 Lorelay Pricop Florescu**  
*Project Manager & Software Engineer*  
Desarrollo y entrenamiento de modelo  
Graduada en Tecnologías Interactivas. Diplomada en Gestión de Proyectos y Agile.
Estudiante de Máster en Inteligencia Artificial.  
✉️ [lorelaypricop@gmail.com](mailto:lorelaypricop@gmail.com) | [LinkedIn](https://www.linkedin.com/in/lorelaypricop)

---

**👤 Daniel Doval Frutos**  
*Hardware Engineer*  
Desarrollador de aplicación móvil y apoyo en el entrenamiento de modelo  
Graduado en Ing. Informática (Hardware).  
✉️ [danieldoval99@gmail.com](mailto:danieldoval99@gmail.com)

---


