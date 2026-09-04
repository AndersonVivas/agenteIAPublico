ARG IMAGE
FROM $IMAGE

WORKDIR /opt/build/

# Copy build artifacts
COPY ./build/PROJECT_NAME.txt /opt/build/
COPY ./build/PROJECT_VERSION.txt /opt/build/
COPY ./build/libs/*.jar /opt/build/

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health/liveness || exit 1

# Use exec form to ensure signals are properly forwarded
ENTRYPOINT ["java", "-jar"]
CMD ["/opt/build/app.jar"]

