Test Plan Generator

The Test Plan Generator is a Java-based automation tool designed to parse EDI maps in Word (.docx) format and automatically generate structured test plans. By reducing manual effort and ensuring consistency, it streamlines the internal testing phase and accelerates project deployment.

🚀 Features

	• Automated Case Generation: Parses EDI map paragraphs and tables to extract test conditions, IDs, and expected results.
	• Dual Operation Modes: Supports New projects (First Install) and Enhancements (Highlight-based parsing).
	• External Testing Support: Allows for modifications and additional cases during client-facing testing phases.
	• Custom Case Management: Manually add or remove custom cases using a LIFO (Last-In-First-Out) stack.
	• Flexible Formatting: Customizable column ordering for exported Excel test plans.
	• Smart Logic: Automatically generates multiple scenarios for "missing," "blank," or "invalid" conditions and handles standard validation tables.

🛠 Installation & Setup

	1. Download: Obtain the release ZIP file.
	2. Directory: Place and extract the ZIP in C:\Documents\TestPlanGenerator.
	3. Launch: Double-click the .jar file to run the application (ensure Java 8+ is installed).

📖 How to Use

1. New or Enhancement Project
	1. Select Operation: Choose between New or Enhancement.
	2. Load Map: Browse and select the .docx EDI Map.
	3. Existing Test Plan (Optional): If performing an enhancement, select the existing .xls/.xlsx file. If unavailable, the tool will generate a fresh template.
	4. Preview: Click Generate Cases. Review the list in the Preview area.
	5. Custom Cases: Use the input fields to add manual cases if necessary.
	6. Export: Select a save path, click Generate Test Plan, and enter the ADO Number when prompted.
2. Modifications during External Testing
Used to capture logical changes (additions, deletions, modifications) during client testing.
	1. Set operation to Enhancement.
	2. Load the map with current changes (highlighted).
	3. Point to the original test plan used prior to external testing.
	4. Enter the same ADO Number. The tool will append cases under an "External Testing" tag (e.g., #1).

🧠 Working Logic

The application follows OM mapping standards to identify test cases:
	• Keywords: Identifies cases based on "Create ED" or "Create Discard" keywords.
	• Structure: * Preceding Paragraph: EDI Case Condition.
		○ Succeeding Paragraph: Split by the first comma into Case ID and Case Message.
	• Enhancements: Only processes text that is highlighted in the Word document.
		○ Strikethrough + Highlight = Deletion.
		○ Highlight only = New Case.
		○ Text change in condition/message = Modified Case.

🔧 Technical Details

	• Language: Java SE 1.8 (JVM)
	• UI Framework: Java Swing / AWT
	• Build Tool: Maven
	• Libraries: Apache POI (for Word/Excel processing)

