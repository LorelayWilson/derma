package com.example.dermaApp

import androidx.compose.ui.graphics.Color

data class DiseaseInfo(
    val name: String,
    val riskLevel: String,
    val riskDescription: String,
    val whatIsDescription: String,
    val howToTreatDescription: String,
    val probability: Float = 0f,
    val colorIndicator: Color
)

val HighRiskColor = Color(0xFFE53935)
val ModerateRiskColor = Color(0xFFFFC107)
val LowRiskColor = Color(0xFF66BB6A)
val BenignColor = Color(0xFF4CAF50)

fun getDiseaseInfo(): List<DiseaseInfo> {
    return listOf(
        DiseaseInfo(
            name = "Eczema",
            riskLevel = "Bajo",
            riskDescription = "No tiene riesgo. Provoca molestias, y el rascado continuo podría provocar infecciones.",
            whatIsDescription = "Es una afección que hace que la piel se seque, pique y se inflame. \n" +
                    "Es común en los niños pequeños, pero puede manifestarse a cualquier edad. \n" +
                    "Puede mejorar o empeorar con el tiempo, pero es usualmente una enfermedad crónica.\n" +
                    "No es una enfermedad contagiosa.",
            howToTreatDescription = "Acude a tu dermatólogo si el picor impide dormir o si la erupción se extiende.\n" +
                    "Mantén la piel bien hidratada, y evita desencadenantes cómo duchas muy calientes, jabones agresivos, prendas que produzcan rozamiento como la lana...",
            colorIndicator = LowRiskColor
        ),
        DiseaseInfo(
            name = "Benign",
            riskLevel = "Benigno",
            riskDescription = "No tiene riesgo. Dado que es similar a un tumor maligno, su mayor riesgo es pasar por alto un tumor maligno.",
            whatIsDescription = "Son concentraciones de células normales en un solo punto de la piel. No suponen gravedad alguna.\n" +
                    "Lo que los diferencia de los tumores malignos es que no tienen movilidad a tejidos aledaños ni se diseminan a otro órgano.\n" +
                    "Aunque por su vista y tacto puedan parecer verrugas, no son infecciosos.\n" +
                    "Es posible extirparlos por temas de estética o comodidad.",
            howToTreatDescription = "Es preciso acudir a un dermatólogo para confirmar que se trata de un caso benigno: Hazlo cada vez que encuentres un bulto nuevo, o que crezca deprisa, sangre o cambie de color.",
            colorIndicator = BenignColor
        ),
        DiseaseInfo(
            name = "Pigmentation",
            riskLevel = "Bajo",
            riskDescription = "Tiene un bajo riesgo. Algunas de estas enfermedades tornan más susceptible a las quemaduras solares a las zonas de la piel que afecta.",
            whatIsDescription = "La pérdida de pigmento (p. ej., vitíligo) o el exceso de pigmento (p. ej., melasma, hiperpigmentación postinflamatoria) altera el color de la piel en zonas específicas. \n" +
                    "Sucede debido a que los melanocitos se destruyen, dejan de funcionar o trabajan en exceso, produciendo zonas más claras o más oscuras.\n" +
                    "Sin embargo, el vitíligo es una enfermedad autoinmune. La gente que padece vitíligo tiene mayor riesgo de sufrir otras enfermedades autoinmunes.",
            howToTreatDescription = "Aplica crema solar en las zonas de la piel con estas patologías. Usa también ropa que evite la exposición al sol.\n" +
                    "Acude a tu dermatólogo si aprecia rápidos cambios en forma o color. También para preveer la existencia de enfermedades autoinmunes en el caso de padecer vitiligo.",
            colorIndicator = LowRiskColor
        ),
        DiseaseInfo(
            name = "Infectious",
            riskLevel = "Moderado",
            riskDescription = "Riesgo moderado. Si no es tratada, podría derivar en problemas más serios.",
            whatIsDescription = "Sucede cuando bacterias entran en la piel a través de heridas. \n" +
                    "Causan enrojecimiento, dolor e hinchazón. En el caso de un absceso, puede provocar pus. En el de una celulitis, afecta a capas más internas de la piel.",
            howToTreatDescription = "Acude a tu dermatólogo y sigue estrictamente su prescripción de antibióticos. Mantén el área limpia, y lava con regularidad las manos para evitar futuras infecciones.",
            colorIndicator = ModerateRiskColor
        ),
        DiseaseInfo(
            name = "Acne",
            riskLevel = "Bajo",
            riskDescription = "Riesgo bajo. Un acné severo y persistente podría dejar marcas en la piel.",
            whatIsDescription = "El acné ocurre cuando el aceite y la piel muerta tapan los folículos pilosos, produciendo puntos negros, puntos blancos o granos inflamados. Si bien es común en los adolescentes, puede aparecer a cualquier edad.",
            howToTreatDescription = "Lava la zona dos veces al día, y emplea productos específicos para su cuidado. Si no hay una mejora clara a las 6-8 semanas, o si aparecen quistes es momento de visitar a un dermatólogo. Sigue estrictamente el tratamiento que proponga.",
            colorIndicator = LowRiskColor
        ),
        DiseaseInfo(
            name = "Malign",
            riskLevel = "Alto",
            riskDescription = "Riesgo alto. Requiere atención médica inmediata para diagnóstico y tratamiento, ya que puede extenderse a otras partes del cuerpo (metástasis) y ser potencialmente mortal si no se trata a tiempo. La detección temprana es crucial.",
            whatIsDescription = "Un tumor maligno de la piel es un crecimiento anormal y descontrolado de células cutáneas que tienen la capacidad de invadir tejidos cercanos y diseminarse a otras partes del cuerpo (metástasis).\n" +
                    "Los tipos más comunes incluyen el carcinoma basocelular, el carcinoma espinocelular y el melanoma, siendo este último el más peligroso.\n" +
                    "A menudo se presentan como lunares nuevos o existentes que cambian de tamaño, forma o color, heridas que no cicatrizan, o protuberancias inusuales.",
            howToTreatDescription = "La autoexploración regular de la piel es vital. Acude inmediatamente a un dermatólogo ante cualquier lesión sospechosa o cambio en lunares existentes (asimetría, bordes irregulares, color variado, diámetro mayor a 6mm, evolución).\n" +
                    "El tratamiento varía según el tipo y estadio del cáncer, pudiendo incluir cirugía, radioterapia, quimioterapia, inmunoterapia o terapia dirigida. La protección solar es la principal medida preventiva.",
            colorIndicator = HighRiskColor
        ),
        DiseaseInfo(
            name = "Molluscum",
            riskLevel = "Bajo",
            riskDescription = "Generalmente es una afección benigna y autolimitada, especialmente en personas con un sistema inmunitario sano. El principal riesgo es la transmisión a otras personas o a otras partes del cuerpo por contacto directo o rascado, y la posibilidad de sobreinfección bacteriana de las lesiones si se rascan.",
            whatIsDescription = "El molusco contagioso es una infección viral de la piel que causa pequeñas protuberancias redondas, firmes y nacaradas (pápulas) con un hoyuelo característico en el centro.\n" +
                    "Suelen aparecer en grupos y pueden causar picor. Es más común en niños, pero puede afectar a adultos, especialmente aquellos con sistemas inmunitarios debilitados.\n" +
                    "Se transmite por contacto directo piel con piel o al tocar objetos contaminados (toallas, juguetes).",
            howToTreatDescription = "En muchos casos, el molusco contagioso desaparece por sí solo sin tratamiento en meses o incluso años. Para evitar la propagación, no rasques ni toques las lesiones y lávate las manos con frecuencia. Cubre las lesiones con ropa o vendas si es posible.\n" +
                    "Consulta a un dermatólogo si las lesiones son extensas, están en áreas sensibles (como los genitales), causan molestias significativas, o si tienes un sistema inmunitario debilitado. El médico puede recomendar tratamientos como crioterapia (congelación), legrado (raspado), o cremas tópicas para acelerar la curación y prevenir la propagación.",
            colorIndicator = LowRiskColor
        ),
        DiseaseInfo(
            name = "Normal",
            riskLevel = "Ninguno",
            riskDescription = "La piel no presenta anomalías significativas.",
            whatIsDescription = "Piel sana sin signos de enfermedad activa.",
            howToTreatDescription = "Mantener una buena higiene y cuidado de la piel.",
            colorIndicator = BenignColor
        ),
        DiseaseInfo(
            name = "Warts",
            riskLevel = "Bajo",
            riskDescription = "Generalmente inofensivas, pero pueden ser molestas, causar dolor leve o problemas estéticos. Son contagiosas y pueden propagarse a otras partes del cuerpo o a otras personas por contacto directo. Algunas verrugas, especialmente las plantares, pueden causar incomodidad al caminar.",
            whatIsDescription = "Las verrugas son pequeños crecimientos cutáneos causados por una infección del virus del papiloma humano (VPH). Pueden aparecer en cualquier parte del cuerpo, pero son comunes en manos y pies.\n" +
                    "Tienen una superficie rugosa y pueden ser del color de la piel, más claras o más oscuras. A veces presentan pequeños puntos negros (vasos sanguíneos coagulados).\n" +
                    "Existen diferentes tipos de verrugas, como las comunes, plantares (en las plantas de los pies), planas y filiformes.",
            howToTreatDescription = "Muchas verrugas desaparecen sin tratamiento, aunque puede llevar meses o años. Para evitar la propagación, no las toques, rasques ni muerdas, y no compartas objetos personales como toallas o calzado.\n" +
                    "Consulta a un dermatólogo si las verrugas son dolorosas, se multiplican rápidamente, no responden a tratamientos de venta libre, o si tienes un sistema inmunitario debilitado. Los tratamientos médicos incluyen crioterapia, medicamentos tópicos con prescripción, electrocirugía o tratamiento con láser.",
            colorIndicator = LowRiskColor
        )
    )
}

fun getDiseaseInfoMap(): Map<String, DiseaseInfo> {
    return getDiseaseInfo().associateBy { it.name }
}