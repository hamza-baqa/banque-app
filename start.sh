#!/bin/bash

# ============================================
# EuroBank Application Startup Script
# ============================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Project directories
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$PROJECT_ROOT/backend-spring"
FRONTEND_DIR="$PROJECT_ROOT/frontend-blazor"

# Log files
LOG_DIR="$PROJECT_ROOT/logs"
BACKEND_LOG="$LOG_DIR/backend.log"
FRONTEND_LOG="$LOG_DIR/frontend.log"

# PID files
PID_DIR="$PROJECT_ROOT/.pids"
BACKEND_PID="$PID_DIR/backend.pid"
FRONTEND_PID="$PID_DIR/frontend.pid"

# Create necessary directories
mkdir -p "$LOG_DIR"
mkdir -p "$PID_DIR"

# Function to print colored messages
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if a command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to check prerequisites
check_prerequisites() {
    print_info "Checking prerequisites..."

    local missing_deps=0

    # Check Java
    if ! command_exists java; then
        print_error "Java is not installed. Please install Java 11 or higher."
        missing_deps=1
    else
        java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$java_version" -lt 11 ]; then
            print_error "Java version must be 11 or higher. Current version: $java_version"
            missing_deps=1
        else
            print_success "Java found: $(java -version 2>&1 | head -n 1)"
        fi
    fi

    # Check Maven
    if ! command_exists mvn; then
        print_error "Maven is not installed. Please install Maven 3.8 or higher."
        missing_deps=1
    else
        print_success "Maven found: $(mvn -version | head -n 1)"
    fi

    # Check .NET SDK
    if ! command_exists dotnet; then
        print_error ".NET SDK is not installed. Please install .NET SDK 7.0 or higher."
        missing_deps=1
    else
        print_success "dotnet found: $(dotnet --version)"
    fi

    if [ $missing_deps -eq 1 ]; then
        print_error "Missing required dependencies. Please install them and try again."
        exit 1
    fi

    print_success "All prerequisites satisfied!"
}

# Function to check if ports are available
check_ports() {
    print_info "Checking if ports are available..."

    if lsof -Pi :8081 -sTCP:LISTEN -t >/dev/null 2>&1; then
        print_error "Port 8081 is already in use (Backend). Please free the port and try again."
        exit 1
    fi

    if lsof -Pi :5001 -sTCP:LISTEN -t >/dev/null 2>&1; then
        print_error "Port 5001 is already in use (Frontend). Please free the port and try again."
        exit 1
    fi

    print_success "Ports 8081 and 5001 are available!"
}

# Function to stop running processes
stop_services() {
    print_info "Stopping services..."

    if [ -f "$BACKEND_PID" ]; then
        backend_pid=$(cat "$BACKEND_PID")
        if ps -p "$backend_pid" > /dev/null 2>&1; then
            print_info "Stopping backend (PID: $backend_pid)..."
            kill "$backend_pid" 2>/dev/null || true
            rm -f "$BACKEND_PID"
        fi
    fi

    if [ -f "$FRONTEND_PID" ]; then
        frontend_pid=$(cat "$FRONTEND_PID")
        if ps -p "$frontend_pid" > /dev/null 2>&1; then
            print_info "Stopping frontend (PID: $frontend_pid)..."
            kill "$frontend_pid" 2>/dev/null || true
            rm -f "$FRONTEND_PID"
        fi
    fi

    # Kill any remaining processes
    pkill -f "mvn spring-boot:run" 2>/dev/null || true
    pkill -f "dotnet run" 2>/dev/null || true

    print_success "Services stopped!"
}

# Function to start backend
start_backend() {
    print_info "Starting backend (Spring Boot)..."

    cd "$BACKEND_DIR"

    # Clean and package
    print_info "Building backend..."
    mvn clean package -DskipTests > "$BACKEND_LOG" 2>&1 &
    wait $!

    # Start Spring Boot application
    print_info "Launching Spring Boot application..."
    mvn spring-boot:run >> "$BACKEND_LOG" 2>&1 &
    BACKEND_PROCESS_PID=$!
    echo $BACKEND_PROCESS_PID > "$BACKEND_PID"

    # Wait for backend to start
    print_info "Waiting for backend to start (this may take a minute)..."
    local max_attempts=60
    local attempt=0

    while [ $attempt -lt $max_attempts ]; do
        if curl -s http://localhost:8081/eurobank/actuator/health > /dev/null 2>&1; then
            print_success "Backend started successfully!"
            print_info "Backend API: http://localhost:8081/eurobank"
            print_info "Swagger UI: http://localhost:8081/eurobank/swagger-ui.html"
            print_info "H2 Console: http://localhost:8081/eurobank/h2-console"
            return 0
        fi
        sleep 2
        attempt=$((attempt + 1))
        echo -n "."
    done

    echo ""
    print_error "Backend failed to start. Check logs at: $BACKEND_LOG"
    exit 1
}

# Function to start frontend
start_frontend() {
    print_info "Starting frontend (Blazor)..."

    cd "$FRONTEND_DIR"

    # Restore packages
    print_info "Restoring .NET packages..."
    dotnet restore > "$FRONTEND_LOG" 2>&1

    # Start Blazor application
    print_info "Launching Blazor application..."
    dotnet run >> "$FRONTEND_LOG" 2>&1 &
    FRONTEND_PROCESS_PID=$!
    echo $FRONTEND_PROCESS_PID > "$FRONTEND_PID"

    # Wait for frontend to start
    print_info "Waiting for frontend to start..."
    local max_attempts=30
    local attempt=0

    while [ $attempt -lt $max_attempts ]; do
        if curl -s -k https://localhost:5001 > /dev/null 2>&1; then
            print_success "Frontend started successfully!"
            print_info "Frontend URL: https://localhost:5001"
            return 0
        fi
        sleep 2
        attempt=$((attempt + 1))
        echo -n "."
    done

    echo ""
    print_error "Frontend failed to start. Check logs at: $FRONTEND_LOG"
    exit 1
}

# Function to display access information
show_access_info() {
    echo ""
    echo "========================================"
    echo -e "${GREEN}  EuroBank Application Started!${NC}"
    echo "========================================"
    echo ""
    echo -e "${BLUE}Frontend (Blazor):${NC}"
    echo "  URL: https://localhost:5001"
    echo ""
    echo -e "${BLUE}Backend (Spring Boot):${NC}"
    echo "  API Base: http://localhost:8081/eurobank"
    echo "  Swagger UI: http://localhost:8081/eurobank/swagger-ui.html"
    echo "  H2 Console: http://localhost:8081/eurobank/h2-console"
    echo ""
    echo -e "${BLUE}Test Credentials:${NC}"
    echo "  Username: jean.dupont"
    echo "  Password: Demo@2024"
    echo ""
    echo -e "${BLUE}Logs:${NC}"
    echo "  Backend: $BACKEND_LOG"
    echo "  Frontend: $FRONTEND_LOG"
    echo ""
    echo -e "${YELLOW}Press Ctrl+C to stop all services${NC}"
    echo "========================================"
    echo ""
}

# Function to tail logs
tail_logs() {
    tail -f "$BACKEND_LOG" "$FRONTEND_LOG" 2>/dev/null &
    TAIL_PID=$!
}

# Cleanup function
cleanup() {
    echo ""
    print_info "Shutting down..."

    if [ -n "$TAIL_PID" ]; then
        kill "$TAIL_PID" 2>/dev/null || true
    fi

    stop_services
    print_success "All services stopped. Goodbye!"
    exit 0
}

# Trap Ctrl+C
trap cleanup SIGINT SIGTERM

# Main execution
main() {
    echo ""
    echo "========================================"
    echo "  EuroBank Application Launcher"
    echo "========================================"
    echo ""

    # Check if we should stop existing services
    if [ "$1" == "stop" ]; then
        stop_services
        exit 0
    fi

    check_prerequisites
    check_ports
    stop_services  # Stop any existing services

    start_backend
    start_frontend

    show_access_info

    # Keep script running and show logs
    tail_logs

    # Wait indefinitely
    while true; do
        sleep 1
    done
}

# Run main function
main "$@"
