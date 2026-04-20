#!/bin/bash

# Live API Testing and Documentation Script
# Tests against localhost/openmrs and captures real responses

BASE_URL="http://localhost/openmrs/ws/rest/v1"
AUTH="admin:Admin123"
OUTPUT_FILE="/tmp/api-test-results.md"

echo "# Nursing Task Management API - Live Test Results" > $OUTPUT_FILE
echo "" >> $OUTPUT_FILE
echo "**Environment:** localhost/openmrs (Docker)" >> $OUTPUT_FILE
echo "**Test Date:** $(date)" >> $OUTPUT_FILE
echo "**Authentication:** Basic Auth (admin:Admin123)" >> $OUTPUT_FILE
echo "" >> $OUTPUT_FILE
echo "---" >> $OUTPUT_FILE
echo "" >> $OUTPUT_FILE

test_endpoint() {
    local method=$1
    local endpoint=$2
    local data=$3
    local description=$4
    
    echo "## $description" >> $OUTPUT_FILE
    echo "" >> $OUTPUT_FILE
    echo "**Request:** \`$method $endpoint\`" >> $OUTPUT_FILE
    echo "" >> $OUTPUT_FILE
    
    if [ -n "$data" ]; then
        echo "**Payload:**" >> $OUTPUT_FILE
        echo "\`\`\`json" >> $OUTPUT_FILE
        echo "$data" >> $OUTPUT_FILE
        echo "\`\`\`" >> $OUTPUT_FILE
        echo "" >> $OUTPUT_FILE
    fi
    
    # Execute request
    if [ -n "$data" ]; then
        response=$(curl -s -w "\n###HTTP_CODE###%{http_code}" -X $method \
            -H "Content-Type: application/json" \
            -u "$AUTH" \
            -d "$data" \
            "$BASE_URL$endpoint" 2>&1)
    else
        response=$(curl -s -w "\n###HTTP_CODE###%{http_code}" -X $method \
            -H "Content-Type: application/json" \
            -u "$AUTH" \
            "$BASE_URL$endpoint" 2>&1)
    fi
    
    http_code=$(echo "$response" | grep "###HTTP_CODE###" | cut -d'#' -f4)
    body=$(echo "$response" | sed '$d' | sed '/###HTTP_CODE###/d')
    
    echo "**Response (HTTP $http_code):**" >> $OUTPUT_FILE
    echo "\`\`\`json" >> $OUTPUT_FILE
    if [ -n "$body" ]; then
        echo "$body" | python3 -m json.tool 2>/dev/null || echo "$body"
    else
        echo "(empty body)"
    fi >> $OUTPUT_FILE
    echo "\`\`\`" >> $OUTPUT_FILE
    echo "" >> $OUTPUT_FILE
    echo "---" >> $OUTPUT_FILE
    echo "" >> $OUTPUT_FILE
}

# Pre-flight check
echo "Testing connectivity..."
if ! curl -s -u "$AUTH" "$BASE_URL/session" > /dev/null 2>&1; then
    echo "ERROR: Cannot connect to $BASE_URL"
    echo "Please ensure Docker is running: docker ps"
    exit 1
fi

echo "Connected to OpenMRS. Running tests..."

# Test 1: List task templates
test_endpoint "GET" "/ipd/task-templates" "" "1. List Task Templates"

# Test 2: Try to create a template (may fail if no task type concept exists)
CREATE_PAYLOAD='{
  "name": "Test Template - API Doc",
  "description": "Created for API documentation testing",
  "taskTypeUuid": "3d1f8b77-0c6d-4e4b-9a7f-2e1c5d8b9f0a",
  "priority": "ROUTINE",
  "active": true
}'
test_endpoint "POST" "/ipd/task-templates" "$CREATE_PAYLOAD" "2. Create Task Template (with test UUID)"

# Test 3: List task instances
test_endpoint "GET" "/ipd/task-instances" "" "3. List Task Instances (no filter - expect error)"

# Test 4: Get patient list to find valid UUID
test_endpoint "GET" "/ipd/wards" "" "4. List Wards"

# Test 5: Try with invalid UUID
test_endpoint "GET" "/ipd/task-templates/invalid-uuid" "" "5. Get Template with Invalid UUID"

# Test 6: Path traversal test
test_endpoint "GET" "/ipd/task-instances/../../../etc/passwd" "" "6. Path Traversal Test"

# Test 7: Empty request
test_endpoint "POST" "/ipd/task-templates" "" "7. Create Template with Empty Body"

# Test 8: XSS attempt
XSS_PAYLOAD='{
  "name": "<script>alert(1)</script>",
  "taskTypeUuid": "3d1f8b77-0c6d-4e4b-9a7f-2e1c5d8b9f0a",
  "priority": "ROUTINE",
  "active": true
}'
test_endpoint "POST" "/ipd/task-templates" "$XSS_PAYLOAD" "8. XSS Attempt"

echo ""
echo "Testing complete. Results saved to: $OUTPUT_FILE"
echo ""
cat $OUTPUT_FILE
