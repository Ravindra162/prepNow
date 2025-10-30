# Piston Code Execution API Documentation

This document describes the new code execution endpoints integrated with Piston API in the SubmissionService.

## Base URL
```
http://localhost:8083/api/code
```

## Endpoints

### 1. Get Available Runtimes

Get a list of all available programming languages and their versions supported by the Piston API.

**Endpoint:** `GET /api/code/runtimes`

**Response Example:**
```json
[
  {
    "language": "javascript",
    "version": "18.15.0",
    "aliases": ["node-javascript", "node-js", "javascript", "js"],
    "runtime": "node"
  },
  {
    "language": "python",
    "version": "3.10.0",
    "aliases": ["py", "python3"],
    "runtime": null
  },
  {
    "language": "java",
    "version": "15.0.2",
    "aliases": [],
    "runtime": null
  },
  {
    "language": "c++",
    "version": "10.2.0",
    "aliases": ["cpp", "g++"],
    "runtime": "gcc"
  }
]
```

**Use Case:**
- Get the list of supported languages to display in the frontend
- Check available versions for a specific language
- Discover language aliases that can be used

---

### 2. Run Code

Execute code directly using the Piston API without needing to create a submission first.

**Endpoint:** `POST /api/code/run`

**Request Body:**
```json
{
  "language": "javascript",
  "code": "console.log('Hello, World!');",
  "version": "*",
  "stdin": "",
  "args": [],
  "runTimeout": 3000,
  "compileTimeout": 10000,
  "compileMemoryLimit": -1,
  "runMemoryLimit": -1
}
```

**Request Fields:**

| Field | Type | Required | Description | Default |
|-------|------|----------|-------------|---------|
| `language` | String | Yes | Programming language (e.g., "javascript", "python", "java") | - |
| `code` | String | Yes | The source code to execute | - |
| `version` | String | No | SemVer version selector (e.g., "3.10.0", "*") | Latest |
| `stdin` | String | No | Standard input to pass to the program | "" |
| `args` | Array[String] | No | Command-line arguments | [] |
| `runTimeout` | Integer | No | Max execution time in milliseconds | 3000 |
| `compileTimeout` | Integer | No | Max compile time in milliseconds | 10000 |
| `compileMemoryLimit` | Long | No | Max compile memory in bytes (-1 = no limit) | -1 |
| `runMemoryLimit` | Long | No | Max runtime memory in bytes (-1 = no limit) | -1 |

**Response Example (Success):**
```json
{
  "language": "javascript",
  "version": "18.15.0",
  "run": {
    "stdout": "Hello, World!\n",
    "stderr": "",
    "output": "Hello, World!\n",
    "code": 0,
    "signal": null
  },
  "compile": null,
  "message": null
}
```

**Response Example (With Compilation):**
```json
{
  "language": "java",
  "version": "15.0.2",
  "run": {
    "stdout": "Hello from Java!\n",
    "stderr": "",
    "output": "Hello from Java!\n",
    "code": 0,
    "signal": null
  },
  "compile": {
    "stdout": "",
    "stderr": "",
    "output": "",
    "code": 0,
    "signal": null
  },
  "message": null
}
```

**Response Example (Error):**
```json
{
  "language": "python",
  "version": "3.10.0",
  "run": {
    "stdout": "",
    "stderr": "Traceback (most recent call last):\n  File \"main.py\", line 1\n    print('Hello\nSyntaxError: unterminated string literal\n",
    "output": "Traceback (most recent call last):\n  File \"main.py\", line 1\n    print('Hello\nSyntaxError: unterminated string literal\n",
    "code": 1,
    "signal": null
  },
  "compile": null,
  "message": null
}
```

**Response Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `language` | String | The language used for execution |
| `version` | String | The version of the runtime used |
| `run` | Object | Results from the execution stage |
| `run.stdout` | String | Standard output from the program |
| `run.stderr` | String | Standard error from the program |
| `run.output` | String | Combined stdout and stderr in order |
| `run.code` | Integer | Exit code (0 = success, non-zero = error) |
| `run.signal` | String | Signal that terminated the process (or null) |
| `compile` | Object | Results from compilation (for compiled languages) |
| `compile.stdout` | String | Compiler standard output |
| `compile.stderr` | String | Compiler standard error |
| `compile.output` | String | Combined compiler output |
| `compile.code` | Integer | Compiler exit code |
| `compile.signal` | String | Compiler signal |
| `message` | String | Error message if request failed |

---

## Supported Languages

The Piston API supports many languages including:

- **JavaScript/Node.js**: `javascript`, `js`
- **Python**: `python`, `py`
- **Java**: `java`
- **C++**: `cpp`, `c++`
- **C**: `c`
- **C#**: `csharp`, `c#`
- **Go**: `go`
- **Rust**: `rust`
- **Ruby**: `ruby`
- **PHP**: `php`
- **TypeScript**: `typescript`, `ts`
- **Kotlin**: `kotlin`
- **Swift**: `swift`

Use the `/runtimes` endpoint to get the most up-to-date list.

---

## Example Usage

### JavaScript Example

**Request:**
```bash
curl -X POST http://localhost:8083/api/code/run \
  -H "Content-Type: application/json" \
  -d '{
    "language": "javascript",
    "code": "const args = process.argv.slice(2);\nconsole.log(\"Args:\", args);",
    "args": ["hello", "world"]
  }'
```

**Response:**
```json
{
  "language": "javascript",
  "version": "18.15.0",
  "run": {
    "stdout": "Args: [ 'hello', 'world' ]\n",
    "stderr": "",
    "output": "Args: [ 'hello', 'world' ]\n",
    "code": 0,
    "signal": null
  }
}
```

### Python with Input Example

**Request:**
```bash
curl -X POST http://localhost:8083/api/code/run \
  -H "Content-Type: application/json" \
  -d '{
    "language": "python",
    "code": "name = input(\"Enter name: \")\nprint(f\"Hello, {name}!\")",
    "stdin": "Alice"
  }'
```

**Response:**
```json
{
  "language": "python",
  "version": "3.10.0",
  "run": {
    "stdout": "Enter name: Hello, Alice!\n",
    "stderr": "",
    "output": "Enter name: Hello, Alice!\n",
    "code": 0,
    "signal": null
  }
}
```

### Java Example

**Request:**
```bash
curl -X POST http://localhost:8083/api/code/run \
  -H "Content-Type: application/json" \
  -d '{
    "language": "java",
    "code": "public class Main { public static void main(String[] args) { System.out.println(\"Hello from Java!\"); } }"
  }'
```

---

## Integration with Frontend

### In the CodingQuestion Component

Update the `handleRunCode` function in `/frontend/src/components/CodingQuestion.jsx`:

```javascript
const handleRunCode = async () => {
  setIsRunning(true);
  
  try {
    const response = await fetch('http://localhost:8083/api/code/run', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        language: language,
        code: code,
        stdin: '', // Add input field if needed
        runTimeout: 3000,
      }),
    });
    
    const result = await response.json();
    
    if (result.run.code === 0) {
      // Success
      console.log('Output:', result.run.stdout);
    } else {
      // Error
      console.error('Error:', result.run.stderr);
    }
    
    // Run against test cases
    if (question?.testCases) {
      const testResults = await Promise.all(
        question.testCases.map(async (testCase) => {
          const testResponse = await fetch('http://localhost:8083/api/code/run', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              language: language,
              code: code,
              stdin: testCase.input,
              runTimeout: 3000,
            }),
          });
          
          const testResult = await testResponse.json();
          
          return {
            input: testCase.input,
            expectedOutput: testCase.expectedOutput,
            actualOutput: testResult.run.stdout.trim(),
            passed: testResult.run.stdout.trim() === testCase.expectedOutput.trim(),
            executionTime: 0, // Piston doesn't provide this
          };
        })
      );
      
      setTestResults(testResults);
    }
    
  } catch (error) {
    console.error('Execution error:', error);
  } finally {
    setIsRunning(false);
    setShowTestCases(true);
  }
};
```

---

## Configuration

The Piston API URL can be configured in `application.properties`:

```properties
# Piston API Configuration for Code Execution
piston.api.url=https://emkc.org/api/v2/piston
```

**Note:** The default configuration uses the public Piston API hosted by Engineer Man. For production use, consider:
- Self-hosting Piston: https://github.com/engineer-man/piston
- Rate limiting and caching
- Authentication and authorization

---

## Error Handling

The API handles errors gracefully:

1. **Invalid Language**: Returns error with message
2. **Timeout**: Code execution is terminated after the specified timeout
3. **Compilation Error**: Returns compile stage errors
4. **Runtime Error**: Returns run stage errors with stderr
5. **API Failure**: Returns error response with message

Always check:
- `run.code` for exit code (0 = success)
- `run.stderr` for error messages
- `compile.code` for compilation status (if applicable)

---

## Performance Considerations

- **Timeout Defaults**: 3 seconds for execution, 10 seconds for compilation
- **Memory Limits**: Default is no limit (-1), set appropriate limits for production
- **Concurrent Executions**: Piston handles multiple requests, but consider rate limiting
- **Caching**: Consider caching runtime information (changes infrequently)

---

## Security Notes

⚠️ **Important Security Considerations:**

1. **Code Validation**: Always validate and sanitize user code before execution
2. **Resource Limits**: Set appropriate timeout and memory limits
3. **Rate Limiting**: Implement rate limiting to prevent abuse
4. **Authentication**: Add authentication for production use
5. **Network Isolation**: Consider running Piston in an isolated environment
6. **Input Sanitization**: Sanitize stdin and args to prevent injection attacks

---

## Testing

### Test the Runtimes Endpoint

```bash
curl http://localhost:8083/api/code/runtimes
```

### Test Code Execution

```bash
curl -X POST http://localhost:8083/api/code/run \
  -H "Content-Type: application/json" \
  -d '{
    "language": "python",
    "code": "print(\"Hello, World!\")"
  }'
```

---

## Future Enhancements

- Add support for multiple test case batch execution
- Implement code execution history and analytics
- Add support for custom language configurations
- Implement result caching for identical code submissions
- Add websocket support for real-time execution feedback

