# EDI 852 to CSV Converter

This is a Spring Boot application that automatically converts EDI 852 (Product Activity Data) files into CSV format. The application monitors a designated input directory, processes incoming EDI files on a scheduled basis, and outputs structured CSV files with extracted data.

## Overview

EDI 852 is an electronic data interchange standard used for transmitting product activity data in retail and supply chain environments. This application simplifies the conversion of EDI 852 documents into a more accessible CSV format while maintaining data integrity.

## Features

- **Automated Scheduling**: Monitors input directory every 10 seconds for new EDI files
- **EDI 852 Parsing**: Extracts line items (LIN) and shipment data (SN1) segments
- **CSV Generation**: Converts EDI data into structured CSV format with the following columns:
  - Line Number
  - Buyer Part Number
  - Quantity
  - Unit of Measure
- **File Management**: Automatically moves processed files to an archive directory
- **Error Logging**: Comprehensive logging using SLF4J for debugging and monitoring
- **Directory Auto-creation**: Automatically creates required directories if they don't exist

## Project Structure

```
src/main/java/com/example/ediprocessor/
├── EdiProcessorApplication.java      # Spring Boot entry point with scheduling enabled
├── Scheduler/
│   └── EDIFileParserScheduler.java   # Scheduled task for monitoring and processing EDI files
└── Service/
    └── EDIFileParserService.java     # Core EDI parsing and CSV generation logic
```

## How It Works

1. **EDIFileParserScheduler**: Runs every 10 seconds (configurable via `@Scheduled` annotation)
   - Scans the `inputFilesDir` directory for `.edi` files
   - Validates file format
   - Invokes the parsing service
   - Moves processed files to `processedFilesDir`

2. **EDIFileParserService**: Handles EDI parsing logic
   - Reads EDI file content line by line
   - Splits content by segment terminators (`~`)
   - Extracts data from LIN (line item) and SN1 (shipment information) segments
   - Writes structured data to CSV output file

## Directory Structure

The application uses three directories:

- **inputFilesDir**: Location where EDI files should be placed for processing
- **processedFilesDir**: Archive directory for processed EDI files
- **outputCSVFilesDir**: Location where generated CSV files are saved

Directories are automatically created in the current working directory if they don't exist.

## Installation & Setup

### Prerequisites

- Java 8 or higher
- Maven 3.6+
- Spring Boot 2.x or higher

### Build and Run

```bash
# Clone the repository
git clone https://github.com/abuthahir2007/EdiProcessor.git

# Navigate to project directory
cd EdiProcessor

# Build the project
mvn clean build

# Run the application
mvn spring-boot:run
```

Or after building:

```bash
java -jar target/EdiProcessor-0.0.1-SNAPSHOT.jar
```

## Configuration

To modify the scheduling interval, edit the `@Scheduled` annotation in `EDIFileParserScheduler.java`:

```java
@Scheduled(fixedRate = 10000)  // Time in milliseconds (10 seconds)
public void findEDIFile() {
    // ...
}
```

## EDI 852 Format Expected

The application expects EDI files with the following segment structure:

```
ISA*00...~
GS*...~
ST*852*...~
LIN*<LineNumber>**<BuyerPartNumber>~
SN1*<Quantity>*<UnitOfMeasure>~
...
SE*...~
GE*...~
IEA*...~
```

**Extracted Fields:**
- **LIN segment**: Line Number (position 1), Buyer Part Number (position 3)
- **SN1 segment**: Quantity (position 1), Unit of Measure (position 2)

## Example CSV Output

```
LineNumber,BuyerPartNumber,Quantity,UnitOfMeasure
001,ABC123,100,EA
002,DEF456,50,CA
```

## Error Handling

- Invalid file formats are logged but still processed
- Processing errors are logged with details for troubleshooting
- Files are moved to `processedFilesDir` even if parsing fails (to prevent infinite reprocessing)

## Logging

The application uses SLF4J for logging. Check logs for:
- File discovery status
- Successful CSV generation confirmation
- File movement errors

## Future Enhancements

- Support for additional EDI segments and standards
- Configurable scheduling intervals via properties file
- REST API for on-demand file processing
- Database integration for processed file tracking
- Advanced error handling with retry mechanisms
