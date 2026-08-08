#!/usr/bin/env python3
from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
errors = []

required = [
    'AGENTS.MD', '.agent/PLAN.MD', '.agent/CONVENTIONS.MD',
    '.agent/specs/DW-001-platform-foundation.md',
    'docs/00-project-context/PROJECT-BRIEF.md',
    'docs/00-project-context/ARCHITECTURE-DECISIONS.md',
    'docs/00-project-context/FEATURE-INDEX.md',
    'docs/adr/ADR-001-rest-grpc-kafka.md',
    'docs/adr/ADR-002-keycloak-authorization.md',
    'docs/superpowers/specs/2026-08-06-library-first-typed-serialization-design.md',
    'docs/superpowers/plans/2026-08-06-library-first-typed-serialization.md',
    'docs/superpowers/plans/2026-08-06-dw001-format-utils-exceptions.md',
    'docs/superpowers/specs/2026-08-06-platform-observability-auto-propagation-design.md',
    'docs/superpowers/plans/2026-08-06-platform-observability-auto-propagation.md',
    'docs/superpowers/plans/2026-08-06-dw001-null-i18n-preconditions.md',
    'backend/pom.xml',
    'backend/platform/be-platform-starter/pom.xml',
    'backend/platform/be-platform-starter/src/main/resources/platform-kafka.yml',
    'backend/platform/be-platform-starter/src/main/java/com/dnnthanh/wallet/be/platform/autoconfigure/PlatformObservabilityAutoConfiguration.java',
    'backend/platform/be-platform-starter/src/main/java/com/dnnthanh/wallet/be/platform/trace/PlatformKafkaObservationBeanPostProcessor.java',
    'backend/platform/be-platform-starter/src/main/java/com/dnnthanh/wallet/be/platform/constant/PlatformInvariantMessages.java',
    'backend/platform/be-platform-starter/src/main/java/com/dnnthanh/wallet/be/platform/i18n/I18nConstants.java',
    'backend/platform/be-platform-cache-starter/pom.xml',
    'backend/Dockerfile.runtime',
    'backend/Dockerfile.service',
    'backend/docker/runtime-entrypoint.sh',
    'backend/docker/service-entrypoint.sh',
    'Dockerfile.backend',
    'docker-compose.yml',
    'compose/infrastructure.yml',
    'compose/backend/all.yml',
    'compose-up.sh',
]
for path in required:
    if not (ROOT / path).is_file():
        errors.append(f'missing required file: {path}')

# DW-001 deliberately mirrors the E-commerce reference repository shape.
if (ROOT / 'backend/be-platform-foundation').exists():
    errors.append('legacy backend/be-platform-foundation must be replaced by backend/platform starters')

brief_path = ROOT / 'docs/00-project-context/PROJECT-BRIEF.md'
brief = brief_path.read_text(encoding='utf-8') if brief_path.exists() else ''
if 'E-commerce repository is a REFERENCE project only' not in brief:
    errors.append('project brief must explicitly mark E-commerce as reference-only')

agents_path = ROOT / 'AGENTS.MD'
agents = agents_path.read_text(encoding='utf-8') if agents_path.exists() else ''
for needle in [
    'Keycloak/OIDC', 'gRPC', 'Kafka', 'Double-entry ledger', 'develop',
    'StringRedisTemplate', 'spring.application.name', 'MapStruct', 'Lombok',
    'platform-kafka.yml', 'traceparent', '@Scheduled',
    'ContextPropagatingTaskDecorator', 'RestClient.Builder', 'Platform-owned tracing',
    'Objects.isNull', 'named constants', 'invariant exceptions'
]:
    if needle not in agents:
        errors.append(f'AGENTS.MD missing convention: {needle}')

conventions_path = ROOT / '.agent/CONVENTIONS.MD'
conventions = conventions_path.read_text(encoding='utf-8') if conventions_path.exists() else ''
for needle in [
    'Platform-owned distributed tracing', 'traceparent', 'tracestate',
    'ContextPropagatingTaskDecorator', 'setObservationEnabled', 'RestClient.Builder',
    'Objects.isNull', 'named constants', 'invariant exceptions'
]:
    if needle not in conventions:
        errors.append(f'.agent/CONVENTIONS.MD missing convention: {needle}')

backend_pom_path = ROOT / 'backend/pom.xml'
backend_pom = backend_pom_path.read_text(encoding='utf-8') if backend_pom_path.exists() else ''
for dependency in ['commons-lang3', 'commons-collections4', 'commons-codec']:
    if dependency not in backend_pom:
        errors.append(f'backend/pom.xml must provide shared utility dependency: {dependency}')
if 'spotless-maven-plugin' not in backend_pom:
    errors.append('backend/pom.xml must enforce Java formatting with spotless-maven-plugin')

for pom in [
    ROOT / 'backend/pom.xml',
    ROOT / 'backend/platform/be-platform-starter/pom.xml',
    ROOT / 'backend/platform/be-platform-cache-starter/pom.xml',
]:
    if pom.exists():
        try:
            ET.parse(pom)
        except Exception as exc:
            errors.append(f'{pom.relative_to(ROOT)} is invalid XML: {exc}')

# Runnable services scan only their bounded context plus shared platform configuration.
# Scanning the entire wallet root also discovers optional platform adapters (Kafka, request-scoped
# security helpers, etc.) and can make unrelated services fail during ApplicationContext startup.
platform_config_package = 'com.dnnthanh.wallet.be.platform.autoconfigure'
for application_path in ROOT.glob('backend/services/be-*/src/main/java/**/*Application.java'):
    application = application_path.read_text(encoding='utf-8')
    package_match = re.search(r'(?m)^package\s+([\w.]+);', application)
    if package_match is None:
        errors.append(f'runnable service application missing package declaration: {application_path.relative_to(ROOT)}')
        continue

    service_package = package_match.group(1)
    if 'scanBasePackages = "com.dnnthanh.wallet.be"' in application:
        errors.append(
            f'runnable service must not scan the entire shared wallet root: '
            f'{application_path.relative_to(ROOT)}'
        )
    for required_package in [service_package, platform_config_package]:
        if f'"{required_package}"' not in application:
            errors.append(
                f'runnable service component scan must include {required_package}: '
                f'{application_path.relative_to(ROOT)}'
            )

observability_config_path = ROOT / 'backend/platform/be-platform-starter/src/main/java/com/dnnthanh/wallet/be/platform/autoconfigure/PlatformObservabilityAutoConfiguration.java'
observability_config = observability_config_path.read_text(encoding='utf-8') if observability_config_path.exists() else ''
for needle in [
    'platformKafkaObservationBeanPostProcessor',
    'platformSchedulingConfigurer',
    'ContextPropagatingTaskDecorator',
    'platformThreadPoolTaskExecutorCustomizer',
    'platformSimpleAsyncTaskExecutorCustomizer',
]:
    if needle not in observability_config:
        errors.append(f'PlatformObservabilityAutoConfiguration missing platform tracing hook: {needle}')

kafka_observation_path = ROOT / 'backend/platform/be-platform-starter/src/main/java/com/dnnthanh/wallet/be/platform/trace/PlatformKafkaObservationBeanPostProcessor.java'
kafka_observation = kafka_observation_path.read_text(encoding='utf-8') if kafka_observation_path.exists() else ''
for needle in [
    'setObservationEnabled(true)',
    'setObservationRegistry(observationRegistry)',
    'setRecordObservationsInBatch(true)',
]:
    if needle not in kafka_observation:
        errors.append(f'Kafka platform tracing must configure: {needle}')

if (ROOT / 'backend/platform/be-platform-starter/src/main/java/com/dnnthanh/wallet/be/platform/trace/TracePropagationConfiguration.java').exists():
    errors.append('legacy TracePropagationConfiguration must not coexist with platform observability auto-configuration')

# Enforce typed Redis/Kafka, configuration-over-hardcoding, common utility reuse, and no feature-local tracing.
approved_manual_json = {
    Path('backend/platform/be-platform-starter/src/main/java/com/dnnthanh/wallet/be/platform/outbox/OutboxPayloadCodec.java'),
}
manual_blank_patterns = [
    re.compile(r'\b([A-Za-z_]\w*)\s*==\s*null\s*\|\|\s*\1\.isBlank\(\)'),
    re.compile(r'\b([A-Za-z_]\w*)\s*!=\s*null\s*&&\s*!\s*\1\.isBlank\(\)'),
]
manual_empty_patterns = [
    re.compile(r'\b([A-Za-z_]\w*)\s*==\s*null\s*\|\|\s*\1\.isEmpty\(\)'),
    re.compile(r'\b([A-Za-z_]\w*)\s*!=\s*null\s*&&\s*!\s*\1\.isEmpty\(\)'),
]
manual_null_patterns = [
    re.compile(r'\b[A-Za-z_]\w*\s*==\s*null\b'),
    re.compile(r'\b[A-Za-z_]\w*\s*!=\s*null\b'),
]
manual_array_empty_patterns = [
    re.compile(r'\b[A-Za-z_]\w*\.length\s*==\s*0\b'),
]
hardcoded_precondition_patterns = [
    re.compile(r'Objects\.requireNonNull\s*\([^;]*?,\s*"'),
    re.compile(r'Validate\.(?:notNull|notBlank|notEmpty|isTrue)\s*\([^;]*?,\s*"'),
    re.compile(r'throw\s+new\s+(?:IllegalArgumentException|IllegalStateException|NullPointerException)\s*\(\s*"'),
]

for path in ROOT.glob('backend/**/src/main/java/**/*.java'):
    relative = path.relative_to(ROOT)
    relative_text = relative.as_posix()
    text = path.read_text(encoding='utf-8')

    if 'StringRedisTemplate' in text:
        errors.append(f'object-cache StringRedisTemplate is forbidden in production code: {relative}')
    if re.search(r'RedisTemplate\s*<\s*String\s*,\s*String\s*>', text):
        errors.append(f'RedisTemplate<String,String> object-cache shortcut is forbidden: {relative}')
    if 'wallet::' in text:
        errors.append(f'hard-coded Redis namespace wallet:: is forbidden; use spring.application.name: {relative}')

    if 'KafkaTemplate<String, String>' in text or 'KafkaTemplate<String, byte[]>' in text:
        errors.append(f'Kafka producer must publish typed objects, not raw String/byte[] values: {relative}')
    if re.search(r'Map\s*<\s*String\s*,\s*Object\s*>\s+payload\b', text):
        errors.append(f'Kafka/event payload must use an explicit typed class/record, not Map<String,Object>: {relative}')

    if any(pattern.search(text) for pattern in manual_blank_patterns):
        errors.append(f'manual null/blank string check found; prefer StringUtils: {relative}')
    if any(pattern.search(text) for pattern in manual_empty_patterns):
        errors.append(f'manual null/empty collection/map check found; prefer existing utility: {relative}')
    if any(pattern.search(text) for pattern in manual_null_patterns):
        errors.append(f'manual reference-null predicate found; prefer Objects.isNull/nonNull or a type-specific utility: {relative}')
    if any(pattern.search(text) for pattern in manual_array_empty_patterns):
        errors.append(f'manual array emptiness check found; prefer ArrayUtils.isEmpty: {relative}')
    if any(pattern.search(text) for pattern in hardcoded_precondition_patterns):
        errors.append(f'hard-coded unchecked precondition message found; use a named constant or typed ErrorCode/i18n: {relative}')

    if relative_text.endswith('/model/CodeEnum.java') or relative_text.endswith('/i18n/I18nCodeEnum.java'):
        if 'instanceof Enum' in text:
            errors.append(f'enum contract must not use runtime instanceof Enum guards: {relative}')

    if relative not in approved_manual_json:
        if 'writeValueAsString(' in text:
            errors.append(f'manual JSON string serialization is forbidden outside approved codecs: {relative}')
        if re.search(r'\bobjectMapper\.readValue\(', text):
            errors.append(f'manual JSON deserialization is forbidden outside approved codecs: {relative}')

    if relative_text.startswith('backend/services/'):
        for forbidden_trace_usage in [
            'traceparent',
            'tracestate',
            'TraceHeaders.TRACE_ID',
            'io.micrometer.tracing.Tracer',
            'setObservationEnabled(',
            'setObservationRegistry(',
            'RestClient.create(',
        ]:
            if forbidden_trace_usage in text:
                errors.append(
                    f'feature/service code must inherit platform tracing instead of using {forbidden_trace_usage}: {relative}'
                )
        if re.search(r'new\s+(ThreadPoolTaskExecutor|SimpleAsyncTaskExecutor)\s*\(', text):
            errors.append(
                f'custom async executor can bypass platform context propagation; use Boot-managed executor or justify via ADR: {relative}'
            )
        if re.search(r'\bnextSpan\s*\(', text):
            errors.append(
                f'feature/service code must not create ordinary distributed trace spans manually: {relative}'
            )

# Every runnable service owns an explicit application name in YAML.
services_root = ROOT / 'backend/services'
for service_dir in sorted(services_root.glob('be-*')) if services_root.exists() else []:
    if not service_dir.is_dir():
        continue

    application_yml = service_dir / 'src/main/resources/application.yml'
    if not application_yml.is_file():
        errors.append(f'runnable service missing src/main/resources/application.yml: {service_dir.relative_to(ROOT)}')
        continue

    yaml = application_yml.read_text(encoding='utf-8')
    nested_name = re.search(r'(?ms)^spring:\s*\n(?:^[ \t]+.*\n)*?^[ \t]+application:\s*\n(?:^[ \t]+.*\n)*?^[ \t]+name\s*:', yaml)
    flat_name = re.search(r'(?m)^spring\.application\.name\s*:', yaml)
    if not (nested_name or flat_name):
        errors.append(f'runnable service must define spring.application.name: {application_yml.relative_to(ROOT)}')

    service_java = '\n'.join(
        path.read_text(encoding='utf-8')
        for path in service_dir.glob('src/main/java/**/*.java')
    )
    uses_kafka = 'KafkaTemplate' in service_java or '@KafkaListener' in service_java
    if uses_kafka and 'platform-kafka.yml' not in yaml:
        errors.append(
            f'Kafka service must import classpath:platform-kafka.yml in application.yml: '
            f'{application_yml.relative_to(ROOT)}'
        )

for path in ROOT.rglob('*'):
    if path.is_file() and path.name != '.gitignore' and '.git' not in path.parts:
        try:
            text = path.read_text(encoding='utf-8')
        except UnicodeDecodeError:
            continue
        forbidden = ('T' + 'BD', 'T' + 'ODO')
        if any(token in text for token in forbidden):
            errors.append(f'placeholder found in {path.relative_to(ROOT)}')

if errors:
    print('REPOSITORY VERIFICATION FAILED')
    for error in errors:
        print(' -', error)
    sys.exit(1)

spec_count = len(list((ROOT / '.agent/specs').glob('DW-*.md')))
print(
    f'REPOSITORY VERIFICATION PASSED: {spec_count} specs present, reference-shaped platform structure, '
    'utility reuse, typed-data conventions, invariant-message rules, and platform-owned tracing rules valid'
)
