package com.fitelyback.backend.modules.equipo;

import com.fitelyback.backend.exception.ApiException;
import com.fitelyback.backend.modules.equipo.dto.MiembroActualizarRequest;
import com.fitelyback.backend.modules.equipo.dto.MiembroCrearRequest;
import com.fitelyback.backend.modules.equipo.dto.MiembroResponse;
import com.fitelyback.backend.modules.tenant.NegocioRepository;
import com.fitelyback.backend.modules.tenant.auth.ProveedorAuth;
import com.fitelyback.backend.modules.tenant.auth.Rol;
import com.fitelyback.backend.modules.tenant.auth.Usuario;
import com.fitelyback.backend.modules.tenant.auth.UsuarioRepository;
import com.fitelyback.backend.modules.ubicaciones.Ubicacion;
import com.fitelyback.backend.modules.ubicaciones.UbicacionRepository;
import com.fitelyback.backend.security.UsuarioAutenticado;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EquipoService {

    private final UsuarioRepository usuarioRepository;
    private final UbicacionRepository ubicacionRepository;
    private final NegocioRepository negocioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<MiembroResponse> listar(UsuarioAutenticado actual) {
        List<String> todasLasSedes = nombresDeSedes(actual.negocioId());
        return usuarioRepository.findByNegocioIdOrderByNombreAscApellidoAsc(actual.negocioId())
                .stream()
                .map(u -> aResponse(u, todasLasSedes, actual))
                .toList();
    }

    @Transactional
    public MiembroResponse crear(MiembroCrearRequest request, UsuarioAutenticado actual) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "El correo ya se encuentra registrado");
        }

        Set<Ubicacion> sedes = resolverSedes(request.ubicacionIds(), request.rol(), actual.negocioId());

        Usuario usuario = Usuario.builder()
                .nombre(request.nombre())
                .apellido(request.apellido())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .proveedor(ProveedorAuth.LOCAL)
                .rol(request.rol())
                .ubicacion(primera(sedes))
                .ubicaciones(sedes)
                .negocio(negocioRepository.getReferenceById(actual.negocioId()))
                .build();

        return aResponse(usuarioRepository.save(usuario), nombresDeSedes(actual.negocioId()), actual);
    }

    @Transactional
    public MiembroResponse actualizar(Long id, MiembroActualizarRequest request, UsuarioAutenticado actual) {
        Usuario usuario = buscar(id, actual.negocioId());

        if (esLaCuentaActual(usuario, actual) && usuario.getRol() != request.rol()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "No puedes cambiar tu propio rol");
        }

        Set<Ubicacion> sedes = resolverSedes(request.ubicacionIds(), request.rol(), actual.negocioId());

        usuario.setNombre(request.nombre());
        usuario.setApellido(request.apellido());
        usuario.setRol(request.rol());
        usuario.getUbicaciones().clear();
        usuario.getUbicaciones().addAll(sedes);

        // Si la tienda actual ya no está entre sus sedes, pasa a la primera de la lista
        Ubicacion tiendaActual = usuario.getUbicacion();
        boolean sigueTeniendoAcceso = tiendaActual != null
                && sedes.stream().anyMatch(s -> s.getId().equals(tiendaActual.getId()));
        if (!sigueTeniendoAcceso) {
            usuario.setUbicacion(primera(sedes));
        }

        // La contraseña solo cambia si se envía
        if (request.password() != null) {
            usuario.setPassword(passwordEncoder.encode(request.password()));
        }

        return aResponse(usuario, nombresDeSedes(actual.negocioId()), actual);
    }

    @Transactional
    public void eliminar(Long id, UsuarioAutenticado actual) {
        Usuario usuario = buscar(id, actual.negocioId());

        if (esLaCuentaActual(usuario, actual)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "No puedes eliminar tu propia cuenta");
        }

        usuarioRepository.delete(usuario);
    }

    private Usuario buscar(Long id, Long negocioId) {
        return usuarioRepository.findByIdAndNegocioId(id, negocioId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Miembro no encontrado"));
    }

    // Valida que cada sede exista en este negocio y que todo STAFF tenga al menos una
    private Set<Ubicacion> resolverSedes(List<Long> ubicacionIds, Rol rol, Long negocioId) {
        List<Long> ids = ubicacionIds == null ? List.of() : ubicacionIds.stream().distinct().toList();

        if (ids.isEmpty() && rol == Rol.STAFF) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Un trabajador debe tener al menos una sede asignada");
        }

        Set<Ubicacion> sedes = new LinkedHashSet<>();
        for (Long ubicacionId : ids) {
            sedes.add(ubicacionRepository.findByIdAndNegocioId(ubicacionId, negocioId)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                            "Ubicación no encontrada: " + ubicacionId)));
        }
        return sedes;
    }

    private Ubicacion primera(Set<Ubicacion> sedes) {
        return sedes.isEmpty() ? null : sedes.iterator().next();
    }

    private List<String> nombresDeSedes(Long negocioId) {
        return ubicacionRepository.findByNegocioIdOrderByNombreAsc(negocioId)
                .stream()
                .map(Ubicacion::getNombre)
                .toList();
    }

    private boolean esLaCuentaActual(Usuario usuario, UsuarioAutenticado actual) {
        return usuario.getEmail().equalsIgnoreCase(actual.email());
    }

    private MiembroResponse aResponse(Usuario u, List<String> todasLasSedes, UsuarioAutenticado actual) {
        Ubicacion tiendaActual = u.getUbicacion();

        // Un ADMIN accede a todas las sedes; un STAFF solo a las asignadas
        List<String> acceso = u.getRol() == Rol.ADMIN
                ? todasLasSedes
                : u.getUbicaciones().stream().map(Ubicacion::getNombre).toList();

        return new MiembroResponse(
                u.getId(),
                u.getNombre(),
                u.getApellido(),
                u.getEmail(),
                u.getRol(),
                tiendaActual != null ? tiendaActual.getNombre() : null,
                u.getUbicaciones().stream().map(Ubicacion::getId).toList(),
                acceso,
                esLaCuentaActual(u, actual)
        );
    }
}