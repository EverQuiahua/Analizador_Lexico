package com.example.analizador_lexico;

import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.util.*;


public class semantico {
    private HelloController helloController;
    private HashMap<String, VariableInfo> variables;
    public List<VariableF> variablesF = new ArrayList<>();

    // Constructor que recibe la instancia de HelloController
    public semantico(HelloController helloController) {
        this.helloController = helloController;
        this.variables = new HashMap<>(); // Inicializa el HashMap
        mostrarExpresiones(); // Llamar a mostrarExpresiones al crear la instancia
    }

    private void mostrarExpresiones() {
        ObservableList<Analisis> lista_expresiones = helloController.getListaExpresiones();

        for (Analisis analisis : lista_expresiones) {
            System.out.println("Expresion: " + analisis.getExpresion());
            System.out.println("Tipo: " + analisis.getTipo());
            System.out.println("Renglon: " + analisis.getRenglon());
            System.out.println("Columna: " + analisis.getColumna());
            System.out.println("--------------------");  // Separador entre cada análisis
        }


        verificarTipos(lista_expresiones);
        verificarOperaciones(lista_expresiones);
        asignacion_op(lista_expresiones);
        coherencia_parametros(lista_expresiones);
        variablesdeclaradas(lista_expresiones);
        controlDeflujo(lista_expresiones);
        control_alcance(lista_expresiones);
        verificarRetornoFuncion(lista_expresiones);
        conversionTipos(lista_expresiones);
    }


    // ---------------------------------------------------------------------------------------------------------
    // 1. Verificación de tipos

    private void verificarTipos(ObservableList<Analisis> lista_expresiones) {
        for (int i = 0; i < lista_expresiones.size(); i++) {
            Analisis analisis = lista_expresiones.get(i);
            String expresion = analisis.getExpresion();
            String tipo = analisis.getTipo();


            // Verifica si es una palabra reservada y si es una declaración de variable
            if (new Palabra_Reservada().isPalabraReservada(expresion) && esTipoDeDato(expresion)) {
                String valor = lista_expresiones.get(i + 3).getExpresion();
                String tipoVariable = expresion; // La palabra reservada (ejemplo: "int", "String", etc.)
                String nombreVariable = lista_expresiones.get(i + 1).getExpresion(); // Nombre de la variable
                // Esperamos que la siguiente expresión sea el nombre de la variable
                if (i + 1 < lista_expresiones.size() && lista_expresiones.get(i + 1).getTipo().equals("Variable")) {
                    variables.put(nombreVariable, new VariableInfo(tipoVariable, nombreVariable,valor));
                    // siguiente elemento de asignación
                    if (i + 2 < lista_expresiones.size() && lista_expresiones.get(i + 2).getExpresion().equals("=")) {
                        // siguiente elemento valor o una variable
                        if (i + 3 < lista_expresiones.size()) {

                            // Verificar si el valor es una función
                            if (lista_expresiones.get(i + 3).getTipo().equals("Función") ||lista_expresiones.get(i + 3).getTipo().equals("Símbolo")  ) {
                                System.out.println("Se ha encontrado una función en la asignación, omitiendo la verificación de asignación.");
                                // Puedes agregar aquí alguna lógica si quieres hacer algo con la función.
                            } else if (lista_expresiones.get(i + 3).getTipo().equals("Variable")) {

                            }
                            else {
                                // Verifica si es un String
                                if (tipoVariable.equals("String")) {
                                    if (!lista_expresiones.get(i + 3).getExpresion().equals("\"") ||
                                            !lista_expresiones.get(i + 5).getExpresion().equals("\"")) {
                                        mostrarAlerta("Error Semántico",
                                                "Asignación inválida: " + tipoVariable + " " + nombreVariable + " = " + valor);

                                        //variables.put(nombreVariable, new VariableInfo(tipoVariable, nombreVariable));
                                    }
                                } else if (tipoVariable.equals("char")) {
                                    String valor2 = lista_expresiones.get(i + 4).getExpresion();
                                    System.out.println(valor2);

                                        // Verifica que el valor sea compatible con un char
                                        if (!esValorCompatible("char", valor2)) {
                                            mostrarAlerta("Error Semántico",
                                                    "Asignación inválida: " + tipoVariable + " " + nombreVariable);

                                            //variables.put(nombreVariable, new VariableInfo(tipoVariable, nombreVariable));
                                        }
                                } else {
                                    // Verifica si el valor es compatible con el tipo de variable
                                    if (!esValorCompatible(tipoVariable, valor)) {
                                        mostrarAlerta("Error Semántico",
                                                "Asignación inválida : " + tipoVariable + " " + nombreVariable + " = " + valor);

                                        //variables.put(nombreVariable, new VariableInfo(tipoVariable, nombreVariable));
                                    }
                                }
                            }
                        }

                    }

                }
            }
        }

        for (int i = 0; i < lista_expresiones.size(); i++) {
            Analisis exprActual = lista_expresiones.get(i);

            // Detectar una asignación de tipo: Variable = Variable
            if (exprActual.getTipo().equals("Variable") &&
                    i + 1 < lista_expresiones.size() &&
                    lista_expresiones.get(i + 1).getExpresion().equals("=") &&
                    i + 2 < lista_expresiones.size() &&
                    lista_expresiones.get(i + 2).getTipo().equals("Variable")) {

                // Obtener el nombre y tipo de la variable destino
                String variableDestino = exprActual.getExpresion();
                String tipoDestino = obtenerTipo(variableDestino);
                System.out.println(tipoDestino);

                // Obtener el nombre y tipo de la variable fuente
                String variableFuente = lista_expresiones.get(i + 2).getExpresion();
                String tipoFuente = obtenerTipo(variableFuente);
                System.out.println(tipoFuente);


                    // Verificar compatibilidad de tipos
                    boolean resultado = sonTiposCompatibles(tipoFuente, tipoDestino);

                    System.out.println("Compatibles: " + resultado); // Esto imprimirá: Compatibles: false
                    if (!sonTiposCompatibles(tipoDestino, tipoFuente)) {
                        String mensaje = "La variable '" + variableDestino +
                                "' de tipo '" + tipoDestino + "' no es compatible con la asignación de '" +
                                variableFuente + "' de tipo '" + tipoFuente + "'.";
                        mostrarAlerta("Error Semántico", mensaje);
                    }


            }
        }
        // Imprime el contenido del mapa 'variables' en consola
        for (Map.Entry<String, VariableInfo> entry : variables.entrySet()) {
            VariableInfo info = entry.getValue();
            System.out.println("Variable Prueba For: " + info.getNombre() + ", Tipo: " + info.getTipo() + ", Valor : " + info.getValor());
        }


    }

    private void verificarOperaciones(ObservableList<Analisis> lista_expresiones) {
        // Inicializamos una variable para almacenar el tipo resultante acumulado
        String tipoResultadoAcumulado = null;

        for (int i = 0; i < lista_expresiones.size(); i++) {
            String expresion = lista_expresiones.get(i).getExpresion();

            // Verificar si la expresión es un operador matemático
            if (esOperadorMatematico(expresion)) {
                // Verificar que tenemos operandos antes y después del operador
                if (i - 1 >= 0 && i + 1 < lista_expresiones.size()) {
                    String operandoIzq;

                    // Si es la primera operación, usamos los operandos iniciales
                    if (tipoResultadoAcumulado == null) {
                        operandoIzq = lista_expresiones.get(i - 1).getExpresion();// Primer operando
                        if (!lista_expresiones.get(i - 1).getTipo().equals("Variable")) {
                            return; // Si el operador izquierdo no es de tipo "variable", termina la verificación
                        }

                    } else {
                        // Usamos el tipo acumulado como el operando izquierdo
                        operandoIzq = tipoResultadoAcumulado;
                    }

                    String operandoDer = lista_expresiones.get(i + 1).getExpresion(); // Segundo operando
                    if (!lista_expresiones.get(i + 1).getTipo().equals("Variable")) {
                        return;
                    }

                    // Obtener los tipos de los operandos
                    String tipoIzq = (tipoResultadoAcumulado == null) ? obtenerTipo(operandoIzq) : tipoResultadoAcumulado;
                    String tipoDer = obtenerTipo(operandoDer);

                    // Verificar si ambos tipos son null
                    if (tipoIzq == null && tipoDer == null) {
                        System.out.println("Ambos operandos son null. No se realiza ninguna verificación de tipos.");
                    } else {
                        // Verificar si los tipos son compatibles para la operación matemática
                        if (!sonTiposCompatibles(tipoIzq, tipoDer)) {
                            mostrarAlerta("Error Semántico",
                                    "Tipos incompatibles en la operación : " + operandoIzq + " " + expresion + " " + operandoDer);
                            return; // Detenemos la verificación si hay un error de tipos
                        } else {
                            System.out.println("Operación válida: " + operandoIzq + " " + expresion + " " + operandoDer);

                            // Calcular el tipo resultante de la operación y acumularlo
                            tipoResultadoAcumulado = obtenerTipoResultado(tipoIzq, tipoDer);
                        }
                    }
                    // Saltamos al siguiente operador después del segundo operando
                    i++; // Avanzamos para que el siguiente ciclo siga desde el próximo operador
                }
            }
        }

        // Validar si el tipo acumulado es compatible con el tipo final esperado (si es necesario)
        if (tipoResultadoAcumulado != null) {
            System.out.println("Tipo final resultante de la expresión: " + tipoResultadoAcumulado);
        }
    }

    private boolean esOperadorMatematico(String expresion) {
        return expresion.equals("+") || expresion.equals("-") || expresion.equals("*") || expresion.equals("/");
    }
    // Función para verificar si una expresión es un tipo de dato válido
    private boolean esTipoDeDato(String expresion) {
        return expresion.equals("int") || expresion.equals("char") || expresion.equals("String") ||
                expresion.equals("double") || expresion.equals("float") || expresion.equals("boolean");
    }

    private String obtenerTipo(String variable) {
        VariableInfo info = variables.get(variable);
        return (info != null) ? info.getTipo() : null; // Retorna el tipo si la variable existe
    }

    private boolean esValorCompatible(String tipoVariable, String valor) {
        switch (tipoVariable) {
            case "int":
                return valor.matches("\\d+"); // Solo números enteros
            case "double":
                return valor.matches("\\d+\\.\\d+"); // Números con decimales
            case "float":
                return valor.matches("\\d+\\.\\d+f"); // Números con decimales terminados en 'f'
            case "char":
                return valor.matches("[a-zA-Z]"); // Un solo carácter entre comillas simples
            case "boolean":
                return valor.equals("true") || valor.equals("false"); // Solo true o false
            case "String":
                return valor.matches("\".*\""); // Cadenas entre comillas dobles
            // Agregar más tipos según sea necesario
            default:
                return false; // Tipo no reconocido
        }
    }

    private boolean sonTiposCompatibles(String tipo1, String tipo2) {
        // Verificar si alguno de los tipos es nulo, en cuyo caso no son compatibles
        if (tipo1 == null || tipo2 == null) {
            return false;
        }

        // Concatenación de strings
        if (tipo1.equals("String") && tipo2.equals("String")) return true;
        if (tipo1.equals("String") && tipo2.equals("char")) return true; // char puede concatenarse con String
        if (tipo1.equals("char") && tipo2.equals("String")) return true; // char puede concatenarse con String

        // Operaciones  con char
        if (tipo1.equals("char") && tipo2.equals("char")) return true; // Operaciones entre dos char

        // Compatibilidad entre números
        if (tipo1.equals("int") && tipo2.equals("int")) return true;
        if (tipo1.equals("double") && (tipo2.equals("int") || tipo2.equals("double"))) return true;
        if (tipo1.equals("int") && tipo2.equals("double")) return true;
        // Compatibilidad con float
        if (tipo1.equals("float") && (tipo2.equals("int") || tipo2.equals("float"))) return true;
        if (tipo1.equals("int") && tipo2.equals("float")) return true;
        if (tipo1.equals("float") && tipo2.equals("double")) return true;
        if (tipo1.equals("double") && tipo2.equals("float")) return true;

        // Si uno es String y el otro no, no son compatibles para operaciones matemáticas
        if (tipo1.equals("String") || tipo2.equals("String")) return false;

        return false; // Por defecto no son compatibles
    }
    // Clase interna para almacenar información de variables
    private static class VariableInfo {
        private String tipo;
        private String nombre;
        private String valor;

        public VariableInfo(String tipo, String nombre, String valor) {
            this.tipo = tipo;
            this.nombre = nombre;
            this.valor = valor;
        }
        public String getTipo() {
            return tipo;
        }
        public String getNombre () {
            return nombre;
        }
        public String getValor () {
            return valor;
        }
    }

    // FIN 1.Verificación de tipos
    // ---------------------------------------------------------------------------------------------------------


    // --------------------------------------------------------------------------------------------------
    //2. Verificación de la existencia de variables y funciones
    private void variablesdeclaradas(ObservableList<Analisis> lista_expresiones) {
        List<VariableFuncion> listaFunciones = new ArrayList<>(); // Lista de funciones con sus variables
        VariableFuncion funcionActual = null;

        for (int i = 0; i < lista_expresiones.size(); i++) {
            Analisis analisis = lista_expresiones.get(i);

            if (analisis.getTipo().equals("Función")) {
                funcionActual = new VariableFuncion(analisis.getExpresion());
                listaFunciones.add(funcionActual);
                Stack<String> pilaLlaves = new Stack<>();
                Stack<String> pilaparentesis = new Stack<>();
                boolean funcionCerrada = false;

                for (int j = i; j < lista_expresiones.size() && !funcionCerrada; j++) {
                    Analisis exprActual = lista_expresiones.get(j);

                    // Detección de parámetros entre paréntesis
                    if (exprActual.getExpresion().equals("(")) {
                        pilaparentesis.push("(");
                    } else if (exprActual.getExpresion().equals(")")) {
                        if (pilaparentesis.size() == 1) {
                            System.out.println("Fin de la función: " + funcionActual.getNombreFuncion() + " en línea " + exprActual.getRenglon());
                        } else {
                            if (!pilaparentesis.isEmpty()) {
                                pilaparentesis.pop();
                            }
                        }
                    }
                    // Detección del inicio del cuerpo de la función
                    if (exprActual.getExpresion().equals("{")) {
                        pilaLlaves.push("{");
                    } else if (exprActual.getExpresion().equals("}")) {
                        if (pilaLlaves.size() == 1) {
                            funcionCerrada = true;
                            System.out.println("Fin de la función: " + funcionActual.getNombreFuncion() + " en línea " + exprActual.getRenglon());
                        } else {
                            if (!pilaLlaves.isEmpty()) {
                                pilaLlaves.pop();
                            }
                        }
                    }

                    if (!pilaLlaves.isEmpty() || !pilaparentesis.isEmpty() ) {
                        // Agregar variables locales y parámetros sin duplicar
                        if (j + 3 < lista_expresiones.size()
                                && (lista_expresiones.get(j).getExpresion().equals("int")
                                || lista_expresiones.get(j).getExpresion().equals("double")
                                || lista_expresiones.get(j).getExpresion().equals("float")
                                || lista_expresiones.get(j).getExpresion().equals("char")
                                || lista_expresiones.get(j).getExpresion().equals("boolean")
                                || lista_expresiones.get(j).getExpresion().equals("String"))
                                && lista_expresiones.get(j + 1).getTipo().equals("Variable")) {
                            String nombreVariable = lista_expresiones.get(j + 1).getExpresion();
                            funcionActual.agregarVariableDeclarada(nombreVariable);
                        }
                    }
                }
            }
        }

        // Imprimir las variables declaradas por función
        for (VariableFuncion funcion : listaFunciones) {
            System.out.println("Función " + funcion.getNombreFuncion() + " tiene variables declaradas: " + funcion.getVariablesDeclaradas());
        }
        // Verificar si hay variables duplicadas por función
        for (VariableFuncion funcion : listaFunciones) {
            List<String> variablesDeclaradas = funcion.getVariablesDeclaradas();
            Set<String> setVariables = new HashSet<>();

            for (String variable : variablesDeclaradas) {
                if (!setVariables.add(variable)) {
                    mostrarAlerta("Error Semántico", "La variable '" + variable + "' ya está declarada en la función " + funcion.getNombreFuncion());
                }
            }
        }
    }

    class VariableFuncion {
        private String nombreFuncion;
        private List<String> variablesDeclaradas;
        private List<String> variablesUsadas;

        public VariableFuncion(String nombreFuncion) {
            this.nombreFuncion = nombreFuncion;
            this.variablesDeclaradas = new ArrayList<>();
            this.variablesUsadas = new ArrayList<>();
        }

        public String getNombreFuncion() {
            return nombreFuncion;
        }

        public List<String> getVariablesDeclaradas() {
            return variablesDeclaradas;
        }

        public List<String> getVariablesUsadas() {
            return variablesUsadas;
        }

        public void agregarVariableDeclarada(String variable) {
            variablesDeclaradas.add(variable); // No hacer verificación, se agregan todas
        }

    }


    // FIN 2. Verificación de la existencia de variables y funciones
    //-------------------------------------------------

    // --------------------------------------------------------------------------------------------------
    // 3. Control de alcance (Scope)
    private void control_alcance(ObservableList<Analisis> lista_expresiones) {
        Map<String, List<String>> variablesFuncion = new HashMap<>(); // Mapa para las variables de la función
        Map<String, List<String>> variablesUsadas = new HashMap<>(); // Mapa para las variables usadas
        String funcionActual = null;

        for (int i = 0; i < lista_expresiones.size(); i++) {
            Analisis analisis = lista_expresiones.get(i);

            if (analisis.getTipo().equals("Función")) {
                funcionActual = analisis.getExpresion();
                Stack<String> pilaLlaves = new Stack<>();
                Stack<String> pilaparentesis = new Stack<>();
                boolean funcionCerrada = false;

                // Inicializamos la lista de variables para la función actual
                variablesFuncion.putIfAbsent(funcionActual, new ArrayList<>());
                variablesUsadas.putIfAbsent(funcionActual, new ArrayList<>()); // Inicializamos la lista de variables usadas

                for (int j = i; j < lista_expresiones.size() && !funcionCerrada; j++) {
                    Analisis exprActual = lista_expresiones.get(j);

                    // Detección de parámetros entre paréntesis
                    if (exprActual.getExpresion().equals("(")) {
                        pilaparentesis.push("(");  // Agregamos llave de apertura
                    } else if (exprActual.getExpresion().equals(")")) {
                        if (pilaparentesis.size() == 1) {
                            System.out.println("Fin de la función: " + funcionActual + " en línea " + exprActual.getRenglon());
                        } else {
                            if (!pilaparentesis.isEmpty()) {
                                pilaparentesis.pop();  // Cerramos la última llave abierta
                            }
                        }
                    }
                    // Solo buscar variables en los parámetros si estamos dentro de los paréntesis
                    if (!pilaparentesis.isEmpty()) {
                        // Detecta la declaración de variable con el patrón: int nombreVariable
                        if (j + 3 < lista_expresiones.size()
                                && lista_expresiones.get(j).getTipo().equals("Palabra Reservada")
                                && lista_expresiones.get(j + 1).getTipo().equals("Variable")) {

                            String variable = lista_expresiones.get(j + 1).getExpresion();
                            if (!variablesFuncion.get(funcionActual).contains(variable)) {
                                variablesFuncion.get(funcionActual).add(variable); // Agrega variable de parámetro si no existe
                            }
                        }
                    }

                    // Detección del inicio del cuerpo de la función
                    if (exprActual.getExpresion().equals("{")) {
                        pilaLlaves.push("{");  // Agregamos llave de apertura
                    } else if (exprActual.getExpresion().equals("}")) {
                        if (pilaLlaves.size() == 1) {
                            funcionCerrada = true;  // La función se ha cerrado
                            System.out.println("Fin de la función: " + funcionActual + " en línea " + exprActual.getRenglon());
                        } else {
                            if (!pilaLlaves.isEmpty()) {
                                pilaLlaves.pop();  // Cerramos la última llave abierta
                            }
                        }
                    }

                    // Solo buscar variables en el cuerpo de la función si estamos dentro de las llaves
                    if (!pilaLlaves.isEmpty()) {
                        // Detecta la declaración de variable con el patrón: int nombreVariable = valor;
                        if (j + 3 < lista_expresiones.size()
                                && (lista_expresiones.get(j).getExpresion().equals("int")
                                || lista_expresiones.get(j).getExpresion().equals("double")
                                || lista_expresiones.get(j).getExpresion().equals("float")
                                || lista_expresiones.get(j).getExpresion().equals("char")
                                || lista_expresiones.get(j).getExpresion().equals("boolean")
                                || lista_expresiones.get(j).getExpresion().equals("String"))
                                && lista_expresiones.get(j + 1).getTipo().equals("Variable")) {
                            String nombreVariable = lista_expresiones.get(j + 1).getExpresion(); // Nombre de la variable

                                // Si no existe, agregar la variable a la lista de declaradas
                                variablesFuncion.get(funcionActual).add(nombreVariable);
                            // Aquí buscamos las variables usadas en expresiones como: variable = ...
                            if (exprActual.getTipo().equals("Variable")) {
                                String variableUsada = exprActual.getExpresion();
                                if (!variablesUsadas.get(funcionActual).contains(variableUsada)) {
                                    variablesUsadas.get(funcionActual).add(variableUsada); // Agrega variable usada si no existe
                                }
                            }
                        }


                    }
                }

                // Verificación de errores semánticos
                verificarVa(funcionActual, variablesFuncion, variablesUsadas);
            }
        }

        // Imprimir el mapa de funciones y sus variables declaradas
        for (Map.Entry<String, List<String>> entry : variablesFuncion.entrySet()) {
            System.out.println("Función: " + entry.getKey() + " tiene variables declaradas: " + entry.getValue());
        }

        // Imprimir el mapa de funciones y sus variables usadas
        for (Map.Entry<String, List<String>> entry : variablesUsadas.entrySet()) {
            System.out.println("Función: " + entry.getKey() + " tiene variables usadas: " + entry.getValue());
        }
    }

    // Metodo para verificar errores semánticos
    private void verificarVa(String funcionActual,
                                            Map<String, List<String>> variablesFuncion,
                                            Map<String, List<String>> variablesUsadas) {
        List<String> variablesDeclaradas = variablesFuncion.get(funcionActual);
        List<String> variablesUtilizadas = variablesUsadas.get(funcionActual);

        for (String variableUsada : variablesUtilizadas) {
            if (!variablesDeclaradas.contains(variableUsada)) {
                // Código para mostrar alerta de error semántico
                mostrarAlerta("Error Semántico", "La variable '" + variableUsada + "' no puede ser usada en la función '" + funcionActual + "'.");
            }
        }
    }

    // FIN 3. Control de alcance (Scope)
    //-------------------------------------------------

    // -----------------------------------------------------------------------------------
    // 4. Verificación de la coherencia de parámetros en funciones

    private void coherencia_parametros(ObservableList<Analisis> lista_expresiones) {
            Stack<String> pilaParentesis = new Stack<>();
            List<FuncionInfo> funcionesList = new ArrayList<>(); // Cambiado a List para permitir funciones repetidas
            String nombreFuncion = null;

            for (int i = 0; i < lista_expresiones.size(); i++) {
                Analisis analisis = lista_expresiones.get(i);

                if (analisis.getTipo().equals("Función")) {
                    // Obtener los elementos anteriores en la lista
                    String publi = lista_expresiones.get(i - 3).getExpresion();
                    String stati = lista_expresiones.get(i - 2).getExpresion();
                    nombreFuncion = analisis.getExpresion();

                    // Determinar si es declaración o llamada
                    String tipoDeclaracion;
                    if ((publi.equals("public") || publi.equals("private")) &&
                            (stati.equals("static") || stati.equals("void") || stati.equals("int") || stati.equals("double"))) {
                        tipoDeclaracion = "Declarada";
                    } else {
                        tipoDeclaracion = "Llamada";
                    }

                    // Crear nueva función con la información adicional y agregar a la lista
                    funcionesList.add(new FuncionInfo(nombreFuncion, 0, "", tipoDeclaracion));
                }

                // Detección de apertura de paréntesis para capturar parámetros
                if (analisis.getExpresion().equals("(") && nombreFuncion != null) {
                    pilaParentesis.push("(");
                }
                // Detección de cierre de paréntesis
                else if (analisis.getExpresion().equals(")") && !pilaParentesis.isEmpty()) {
                    pilaParentesis.pop();
                    if (pilaParentesis.isEmpty()) {
                        nombreFuncion = null; // Reinicia nombre de función una vez que se cierran los paréntesis
                    }
                }

                // Almacenar variables solo dentro de los paréntesis de la función
                if (nombreFuncion != null && !pilaParentesis.isEmpty()) {
                    if (analisis.getTipo().equals("Variable")) {
                        String variable = analisis.getExpresion();
                        // Buscar la última función añadida (la más reciente) en la lista
                        FuncionInfo funcionInfo = funcionesList.get(funcionesList.size() - 1);

                        // Verificar si la variable no está ya en la lista
                        if (!funcionInfo.getVariables().contains(variable)) {
                            // Agregar variable y actualizar número de parámetros
                            String variablesActualizadas = funcionInfo.getVariables().isEmpty()
                                    ? variable
                                    : funcionInfo.getVariables() + ", " + variable;
                            funcionInfo.variables = variablesActualizadas;
                            funcionInfo.numParametro = String.valueOf(funcionInfo.getNumParametroInt() + 1);
                        }
                    }
                }
            }
            for (FuncionInfo funcionInfo : funcionesList) {
                System.out.println("Función: " + funcionInfo.getNombre());
                System.out.println("Número de parámetros: " + funcionInfo.getNumParametro());
                System.out.println("Variables en paréntesis: " + funcionInfo.getVariables());
                System.out.println("Tipo: " + funcionInfo.getTipoDeclaracion());
            }
        Map<String, List<FuncionInfo>> funcionesAgrupadas = new HashMap<>();

        // Agrupar funciones por nombre
        for (FuncionInfo funcionInfo : funcionesList) {
            funcionesAgrupadas
                    .computeIfAbsent(funcionInfo.getNombre(), k -> new ArrayList<>())
                    .add(funcionInfo);
        }

        // Verificar funciones agrupadas
        for (Map.Entry<String, List<FuncionInfo>> entry : funcionesAgrupadas.entrySet()) {
            List<FuncionInfo> funciones = entry.getValue();

            // Comprobar si hay funciones repetidas
            if (funciones.size() > 1) {
                int cantidadDeclaradas = 0;

                for (int i = 0; i < funciones.size(); i++) {
                    FuncionInfo funcA = funciones.get(i);

                    // Contar cuántas veces está declarada la función
                    if (funcA.getTipoDeclaracion().equals("Declarada")) {
                        cantidadDeclaradas++;

                        // Si hay más de una declaración de la misma función, muestra un error semántico
                        if (cantidadDeclaradas > 1) {
                            String mensaje = "Error Semántico: La función '" + funcA.getNombre() +
                                    "' está declarada más de una vez.";
                            mostrarAlerta("Error Semántico", mensaje);
                            break;  // Salir del ciclo si ya detectamos un duplicado
                        }
                    }

                    // Comparar funciones declaradas con llamadas
                    for (int j = 0; j < funciones.size(); j++) {
                        if (i != j) {
                            FuncionInfo funcB = funciones.get(j);

                            // Solo comparar si funcA es una función declarada y funcB es una llamada
                            if (funcA.getTipoDeclaracion().equals("Declarada") && funcB.getTipoDeclaracion().equals("Llamada")) {

                                // Comparar el número de parámetros
                                if (!funcA.getNumParametro().equals(funcB.getNumParametro())) {
                                    String mensaje = "La función '" + funcA.getNombre() +
                                            "' espera " + funcA.getNumParametro() + " parámetros, " +
                                            "pero recibe " + funcB.getNumParametro() + " parámetros.";
                                    mostrarAlerta("Error Semántico", mensaje);
                                }

                                // Comparar los tipos de las variables (asumiendo que las variables están separadas por comas)
                                String[] variablesA = funcA.getVariables().split(", ");
                                String[] variablesB = funcB.getVariables().split(", ");

                                // Comparar tipos de variables
                                for (int k = 0; k < Math.min(variablesA.length, variablesB.length); k++) {
                                    String tipoA = obtenerTipo(variablesA[k].trim());
                                    String tipoB = obtenerTipo(variablesB[k].trim());

                                    if (tipoA != null && tipoB != null && !tipoA.equals(tipoB)) {
                                        String mensaje = "La variable '" + variablesA[k].trim() +
                                                "' en la función '" + funcA.getNombre() + "' se espera que sea de tipo '" +
                                                tipoA + "', pero se ha definido como '" + tipoB + "' en la llamada.";
                                        mostrarAlerta("Error Semántico", mensaje);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        }
    // Clase interna para almacenar información de funciones
    private static class FuncionInfo {
        private String nombre;
        private String numParametro;
        private String variables;
        private String tipoDeclaracion; // Nuevo atributo para indicar si es declaración o llamada

        public FuncionInfo(String nombre, int numParametro, String variables, String tipoDeclaracion) {
            this.nombre = nombre;
            this.numParametro = String.valueOf(numParametro);
            this.variables = variables;
            this.tipoDeclaracion = tipoDeclaracion;
        }

        public String getNombre() {
            return nombre;
        }

        public String getNumParametro() {
            return numParametro;
        }

        public int getNumParametroInt() {
            return Integer.parseInt(numParametro);
        }

        public String getVariables() {
            return variables;
        }

        public String getTipoDeclaracion() {
            return tipoDeclaracion;
        }
    }
    // FIN 4. Verificación de la coherencia de parámetros en funciones
    //-------------------------------------------------

    // -----------------------------------------------------------------------------------
    // 5. Control de flujo de datos

    private void  controlDeflujo(ObservableList<Analisis> lista_expresiones) {
        Stack<String> pilaparentesis = new Stack<>();
        List<String> ListaVariables = new ArrayList<>();
        String est = "";
        for (int j = 0; j < lista_expresiones.size(); j++) {
            Analisis analisis = lista_expresiones.get(j);

            // Verificar si el análisis es un bucle o condicional
            if (analisis.getTipo().equals("Palabra Reservada") &&
                    (analisis.getExpresion().equals("while") ||
                            analisis.getExpresion().equals("for") ||
                            analisis.getExpresion().equals("if") ||
                            analisis.getExpresion().equals("else"))) {

                // Manejo de paréntesis
                if (j + 1 < lista_expresiones.size() && lista_expresiones.get(j + 1).getExpresion().equals("(")) {
                    est = lista_expresiones.get(j).getExpresion();
                    System.out.println(est);
                    pilaparentesis.push("("); // Agrega un paréntesis abierto a la pila
                }
            }

            // Solo buscar variables en los parámetros si estamos dentro de los paréntesis
            if (!pilaparentesis.isEmpty()) {
                // Manejo de paréntesis de cierre
                if (analisis.getExpresion().equals(")")) {
                    if (!pilaparentesis.isEmpty()) {
                        pilaparentesis.pop(); // Quita el paréntesis abierto correspondiente

                        for (int i = 0; i < ListaVariables.size(); i++) {
                            String variable = ListaVariables.get(i); // Obtener la variable actual
                            String tipoV = obtenerTipo(variable); // Obtener el tipo de la variable
                            System.out.println("Tipo de " + variable + ": " + tipoV);

                            // Comprobar con el siguiente elemento si existe
                            if (i + 1 < ListaVariables.size()) {
                                String siguienteVariable = ListaVariables.get(i + 1);
                                String tipoSiguiente = obtenerTipo(siguienteVariable); // Obtener el tipo de la siguiente variable

                                // Verificar la compatibilidad de tipos
                                boolean sonCompatibles = sonTiposCompatibles(tipoV, tipoSiguiente);
                                System.out.println("¿Son compatibles " + tipoV + " y " + tipoSiguiente + "? " + sonCompatibles);
                                // Si no son compatibles, mostrar una alerta
                                if (!sonCompatibles) {
                                    String titulo = "Error Semántico";
                                    String mensaje = "Error: Los tipos '" + tipoV + "' y '" + tipoSiguiente + "' no coinciden  en la estructura : " + est;
                                    mostrarAlerta(titulo, mensaje);
                                }
                            }
                        }

                    }
                } else {
                    // Detecta la declaración de variable
                    if (j + 1 < lista_expresiones.size() &&
                            lista_expresiones.get(j + 1).getTipo().equals("Variable")) {

                        String variable = lista_expresiones.get(j + 1).getExpresion();


                        // Almacena la variable en la pila
                        if (!ListaVariables.contains(variable)) {
                            ListaVariables.add(variable); // Agrega variable de parámetro si no existe
                        }
                    }
                }

            }
        }

    }

    // FIN 5. Control de flujo de datos
    //-------------------------------------------------

    // -----------------------------------------------------------------------------------
    // 6. Verificación de la compatibilidad de tipos en expresiones
    // FIN 6. Verificación de la compatibilidad de tipos en expresiones
    //-------------------------------------------------

    // -----------------------------------------------------------------------------------
    // 7. Asignaciones correctas
    private void asignacion_op(ObservableList<Analisis> lista_expresiones) {
        // Inicializamos una variable para almacenar el tipo resultante acumulado
        String tipoResultadoAcumulado = null;
        String valor = null;
        String tipoVariable = null;

        for (int i = 0; i < lista_expresiones.size(); i++) {
            String expresion = lista_expresiones.get(i).getExpresion();

            // Verificar si la expresión es un operador matemático
            if (esOperadorMatematico(expresion)) {
                // Obtener los operandos antes y después del operador
                if (i - 1 >= 0 && i + 1 < lista_expresiones.size()) {
                    String operandoIzq = lista_expresiones.get(i - 1).getExpresion();
                    String operandoDer = lista_expresiones.get(i + 1).getExpresion();

                    // Si estamos al principio, obtener la variable a la que se asigna el resultado
                    if (i - 3 >= 0) {
                        valor = lista_expresiones.get(i - 3).getExpresion(); // Variable a la que se asigna
                        tipoVariable = lista_expresiones.get(i - 3).getTipo(); // Tipo de la variable
                    }

                    // Obtener los tipos de los operandos izquierdo y derecho
                    String tipoIzq = obtenerTipo(operandoIzq);
                    String tipoDer = obtenerTipo(operandoDer);

                    if (tipoVariable != null && tipoVariable.equals("Variable")) {
                        // Si estamos en la primera operación, obtenemos el tipo de la variable
                        if (tipoResultadoAcumulado == null) {
                            tipoResultadoAcumulado = obtenerTipoResultado(tipoIzq, tipoDer);
                        } else {
                            // Si ya tenemos un resultado acumulado, lo usamos como operando izquierdo
                            tipoResultadoAcumulado = obtenerTipoResultado(tipoResultadoAcumulado, tipoDer);
                        }

                        System.out.println("Tipo resultante acumulado: " + tipoResultadoAcumulado);
                        System.out.println("Tipo de la variable: " + obtenerTipo(valor));

                        // Verificar si los tipos coinciden
                        if (!obtenerTipo(valor).equals(tipoResultadoAcumulado)) {
                            // Mostrar alerta si los tipos no son iguales
                            mostrarAlerta("Error Semántico", "Error: No se puede asignar el resultado de la operación a la variable: " + valor);
                            return; // Detener si hay un error
                        }

                        // Avanzamos al siguiente operador
                        i += 1; // Saltamos el operando derecho que ya se procesó
                    }
                }
            }
        }
    }
    private String obtenerTipoResultado(String tipo1, String tipo2) {
        // Verificar si ambos tipos son iguales
        if (tipo1.equals(tipo2)) {
            return tipo1; // Si son del mismo tipo, el resultado es del mismo tipo
        }
        // Reglas de promoción de tipos en Java
        if (tipo1.equals("double") || tipo2.equals("double")) {
            return "double"; // Si uno de los dos es double, el resultado es double
        } else if (tipo1.equals("float") || tipo2.equals("float")) {
            return "float"; // Si uno de los dos es float y no hay un double, el resultado es float
        } else if (tipo1.equals("long") || tipo2.equals("long")) {
            return "long"; // Si uno de los dos es long y no hay ni float ni double, el resultado es long
        } else if(tipo1.equals("String") || tipo2.equals("String")) {
             return "String"; // Si uno de los dos es String, el resultado será una concatenación y será String
        }
        return "error";
    }
    // FIN 7. Asignaciones correctas
    //-------------------------------------------------

    // -----------------------------------------------------------------------------------
    // 8. Verificación de retornos de funciones

    // Metodo para verificar si el retorno de la función es adecuado
    private void verificarRetornoFuncion(ObservableList<Analisis> lista_expresiones) {
        for (int i = 0; i < lista_expresiones.size(); i++) {
            Analisis analisis = lista_expresiones.get(i);
            String tipo = analisis.getTipo();

            // Verificar si el tipo de la expresión es una función
            if (tipo.equals("Función")) {
                String tipoF = lista_expresiones.get(i - 1).getExpresion(); // Tipo esperado de retorno de la función

                // Seguir recorriendo hasta encontrar el 'return'
                for (int j = i + 1; j < lista_expresiones.size(); j++) {
                    Analisis analisisReturn = lista_expresiones.get(j);
                    String expresionReturn = analisisReturn.getExpresion();
                    String tipoReturn = analisisReturn.getTipo();

                    // Verificar si es una palabra reservada y la expresión es 'return'
                    if (tipoReturn.equals("Palabra Reservada") && expresionReturn.equals("return")) {


                        for (int k = i + 1; k < lista_expresiones.size(); k++) {
                            Analisis analisisParametro = lista_expresiones.get(k);
                            String expresion = analisisParametro.getExpresion();
                            String tipoParametro = analisisParametro.getTipo();

                            // Buscar los parámetros de la función hasta que se cierre el paréntesis
                            if (tipoParametro.equals("Variable")) {
                                // Obtener el tipo de la variable, que se encuentra en la posición anterior
                                String tipoVariable = lista_expresiones.get(k - 1).getExpresion(); // Obtener el tipo desde la posición anterior

                                // Verificar si la variable ya está en la lista
                                boolean existe = variablesF.stream().anyMatch(var -> var.getNombre().equals(expresion));

                                // Si la variable no está en la lista, agregarla
                                if (!existe) {
                                    variablesF.add(new VariableF(expresion, tipoVariable));
                                    variables.put(expresion, new VariableInfo(tipoVariable, expresion,null));
                                }
                            }

                            // Si encontramos un símbolo que cierra el paréntesis, terminamos de buscar
                            if (expresion.equals("return")) {
                                break;
                            }
                        }

                        // Imprimir las variables y sus tipos
                        System.out.println("Variables de la función " + analisis.getExpresion() + ": ");
                        for (VariableF variable : variablesF) {
                            System.out.println(variable);
                        }

                        // Verificar límite
                        if (j + 1 >= lista_expresiones.size()) break;

                        String tipoR = null;
                        String operador;

                        // Verificar si hay un operador en la expresión
                        if (j + 2 < lista_expresiones.size()) {
                            operador = lista_expresiones.get(j + 2).getExpresion();

                        } else {
                            operador = null; // No hay operador
                        }

                        // Verificar si la expresión después de return es una operación
                        if (esOperadorMatematico(operador)) {
                            int indiceOperador = j + 1; // Comienza después del 'return'

                            // Mientras haya operadores y expresiones válidas por procesar
                            while (indiceOperador + 2 < lista_expresiones.size() && esOperadorMatematico(lista_expresiones.get(indiceOperador + 1).getExpresion())) {
                                String operando1 = lista_expresiones.get(indiceOperador).getExpresion();
                                String operando2 = lista_expresiones.get(indiceOperador + 2).getExpresion(); // Siguiente operando

                                // Obtener los tipos de los operandos
                                String tipoOperando1 = obtenerTipoF(operando1);
                                String tipoOperando2 = obtenerTipoF(operando2);

                                // Determinar el tipo resultante de la operación
                                if (tipoR == null) {
                                    tipoR = obtenerTipoResultado(tipoOperando1, tipoOperando2); // Primera operación
                                } else {
                                    tipoR = obtenerTipoResultado(tipoR, tipoOperando2); // Acumular el tipo de la operación
                                }

                                // Avanzar para la siguiente operación
                                indiceOperador += 2; // Salta al siguiente operador
                            }

                        } else {

                            String operando1 = lista_expresiones.get(j + 1).getExpresion();
                            String tipoOperando1 = obtenerTipoF(operando1);

                            tipoR = tipoOperando1;

                        }

                        // Verificar si el tipoF es void y omitir la comparación
                        if (tipoF.equals("void")) {
                            System.out.println("Tipo de retorno 'void', no se realiza comparación.");
                        }
                        // Verificar si el tipoF es boolean y hacer la comparación solo si el tipoR es null
                        else if (tipoF.equals("boolean")) {
                            if (tipoR == null) {
                                // Mostrar alerta de error semántico si el tipoR es null
                                mostrarAlerta("Error Semántico", "El tipo de retorno de la función no puede ser 'null' para boolean.");
                            }
                        }
                        // Realizar la comparación normal para otros tipos
                        else {
                            if (tipoR == null) {
                                // Mostrar alerta de error semántico si el tipoR es null
                                mostrarAlerta("Error Semántico", "El tipo de retorno de la función no puede ser 'null'");
                            }
                            // Comparar el tipo esperado (tipoF) con el tipo real de retorno (tipoR)
                            if (!tipoF.equals(tipoR)) {
                                // Mostrar alerta de error semántico si los tipos son diferentes
                                mostrarAlerta("Error Semántico",
                                        "El tipo de retorno de la función no coincide con el tipo esperado. " +
                                                "Esperado: " + tipoF + ", Encontrado: " + tipoR);
                            } else {
                                // Imprimir mensaje si el tipo de retorno es correcto
                                System.out.println("Tipo de retorno correcto para la función: " + tipoF);
                            }
                        }
                    }
                }
            }
        }
    }
    private String obtenerTipoF(String variableF) {
        // Busca la variableF en la lista de variables
        for (VariableF variable : variablesF) {
            if (variable.getNombre().equals(variableF)) {
                // Retorna el tipo si la variable existe
                return variable.getTipo();
            }
        }
        // Retorna null si no se encontró la variable
        return null;
    }

    public class VariableF {
        private String nombre;
        private String tipo;

        public VariableF(String nombre, String tipo) {
            this.nombre = nombre;
            this.tipo = tipo;
        }

        public String getNombre() {
            return nombre;
        }

        public String getTipo() {
            return tipo;
        }

        @Override
        public String toString() {
            return "Nombre: " + nombre + ", Tipo: " + tipo;
        }
    }



    // FIN 8. Verificación de retornos de funciones
    //-------------------------------------------------

    // -----------------------------------------------------------------------------------
    // 9. Análisis de la sobrecarga de operadores y funciones

    // Aquí va el código para manejar la sobrecarga de operadores
    // y la sobrecarga de funciones en caso de ser permitido por el lenguaje.
    // Se debe identificar cuál versión de la función u operador debe invocarse
    // dependiendo de los tipos de los argumentos.

    // FIN 9. Análisis de la sobrecarga de operadores y funciones
    //-------------------------------------------------

    // -----------------------------------------------------------------------------------
    // 10. Compatibilidad y conversión de tipos
    private void conversionTipos(ObservableList<Analisis> lista_expresiones) {
        for (int i = 0; i < lista_expresiones.size(); i++) {
            Analisis exprActual = lista_expresiones.get(i);

            // Verificamos la estructura de la conversión
            if (exprActual.getTipo().equals("Variable") &&
                    i + 1 < lista_expresiones.size() &&
                    lista_expresiones.get(i + 1).getExpresion().equals("=") &&
                    i + 2 < lista_expresiones.size() &&
                    lista_expresiones.get(i + 2).getExpresion().equals("(") && // Verificamos el inicio de la conversión
                    i + 3 < lista_expresiones.size() &&
                    esTipoDeDato(lista_expresiones.get(i + 3).getExpresion()) && // Verificar tipo de dato válido
                    i + 4 < lista_expresiones.size() &&
                    lista_expresiones.get(i + 4).getExpresion().equals(")") &&
                    i + 5 < lista_expresiones.size() &&
                    lista_expresiones.get(i + 5).getTipo().equals("Variable")) {

                // Obtener el tipo de conversión (por ejemplo: int, double, etc.)
                String tipoConversion = lista_expresiones.get(i + 3).getExpresion();

                // Obtener el tipo de la variable destino
                String variableDestino = exprActual.getExpresion();
                String tipoDestino = obtenerTipo(variableDestino);
                System.out.println("Tipo de destino: " + tipoDestino);

                // Obtener el tipo de la variable fuente
                String variableFuente = lista_expresiones.get(i + 5).getExpresion();
                String tipoFuente = obtenerTipo(variableFuente);
                System.out.println("Tipo de fuente: " + tipoFuente);

                // Verificar si la conversión es válida y segura
                if (!esConversionSegura(tipoFuente, tipoConversion) || !esConversionSegura(tipoConversion, tipoDestino)) {
                    String mensaje = "Conversión inválida: No se puede convertir de '" + tipoFuente +
                            "' a '" + tipoConversion + "', o de '" + tipoConversion +
                            "' a '" + tipoDestino + "'.";
                    mostrarAlerta("Error Semántico", mensaje);
                } else {
                    System.out.println("Conversión válida de " + tipoFuente + " a " + tipoConversion + ", y luego a " + tipoDestino + ".");
                }
            }
        }
    }

    private boolean esConversionSegura(String tipoOrigen, String tipoDestino) {
        // Manejo de casos nulos
        if (tipoOrigen == null || tipoDestino == null) {
            return false; // Conversión insegura si alguno de los tipos es nulo
        }

        // Conversión segura entre el mismo tipo
        if (tipoOrigen.equals(tipoDestino)) {
            return true;
        }

        // Reglas de conversión implícita
        switch (tipoOrigen) {
            case "int":
                // Un int puede convertirse implícitamente a float o double (sin pérdida de precisión)
                return tipoDestino.equals("float") || tipoDestino.equals("double");

            case "float":
                // Un float puede convertirse implícitamente a double (sin pérdida de precisión)
                if (tipoDestino.equals("double")) return true;
                // Convertir un float a int es inseguro (se requiere conversión explícita)
                if (tipoDestino.equals("int")) {
                    System.out.println("Advertencia: Posible pérdida de precisión al convertir de float a int.");
                    return false;
                }
                break;

            case "double":
                // Un double puede convertirse explícitamente a float o int (se requiere advertencia)
                if (tipoDestino.equals("float") || tipoDestino.equals("int")) {
                    System.out.println("Advertencia: Posible pérdida de precisión al convertir de double a " + tipoDestino + ".");
                    return false;
                }
                break;

            case "char":
                // Un char puede convertirse implícitamente a int (representación numérica del carácter)
                return tipoDestino.equals("int") || tipoDestino.equals("String");

            case "String":
                // Un String no puede convertirse a otros tipos, excepto a otro String
                return tipoDestino.equals("String");

            case "boolean":
                // Un boolean no es convertible a otros tipos
                return tipoDestino.equals("boolean");

            default:
                // Tipo desconocido, conversión insegura
                return false;
        }

        // Por defecto, si no coincide con ninguna regla, la conversión no es segura
        return false;
    }
    




    // FIN 10. Compatibilidad y conversión de tipos
    //-------------------------------------------------

    // -----------------------------------------------------------------------------------
    // 11. Gestión de declaraciones y definiciones múltiples



    // Aquí va el código para detectar declaraciones duplicadas de variables o funciones.
    // Si una variable o función es declarada dos veces en el mismo ámbito,
    // el compilador debe emitir un error semántico.

    // FIN 11. Gestión de declaraciones y definiciones múltiples
    //-------------------------------------------------
// 11. Gestión de declaraciones y definiciones múltiples
    // -----------------------------------------------------------------------------------------------------------------------
    // Código para mostrar alerta de error semántico
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
