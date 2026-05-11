package com.barbeariasorrisobarber.agendamento.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.barbeariasorrisobarber.agendamento.exceptions.FileUploadException;

class FileUploadServiceTest {

    private FileUploadService service;
    private Path tempDir;

    @BeforeEach
    void setup() throws IOException, NoSuchFieldException, IllegalAccessException {
        service = new FileUploadService();
        tempDir = Files.createTempDirectory("fu-test-");
        // inject uploadDir
        Field uploadDirField = FileUploadService.class.getDeclaredField("uploadDir");
        uploadDirField.setAccessible(true);
        uploadDirField.set(service, tempDir.toString());
    }

    @AfterEach
    void cleanup() throws IOException {
        Files.walk(tempDir)
            .sorted((a, b) -> b.compareTo(a))
            .forEach(p -> { try { Files.deleteIfExists(p); } catch (Exception e) { /* ignore cleanup errors */ } });
    }

    @Test
    void arquivoExiste_nullOrEmpty_returnsFalse() {
        assertFalse(service.arquivoExiste(null));
        assertFalse(service.arquivoExiste(""));
    }

    @Test
    void getCaminhoCompleto_and_removerArquivo() throws Exception {
        Path p = service.getCaminhoCompleto("x.txt");
        assertTrue(p.endsWith(Paths.get(tempDir.toString(), "x.txt")));

        Path file = tempDir.resolve("to-delete.txt");
        Files.writeString(file, "hello");
        assertTrue(Files.exists(file));
        service.removerArquivo("to-delete.txt");
        assertFalse(Files.exists(file));
    }

    @Test
    void removerImagem_delegatesAndDoesNotThrow() {
        // no exception for non-existent
        assertDoesNotThrow(() -> service.removerImagem("no-such-file.png"));
    }

    @Test
    void salvarImagem_nullOriginalName_throws() {
        MultipartFile mp = new MockMultipartFile("file", null, "image/png", new byte[] {1,2,3});
        FileUploadException ex = assertThrows(FileUploadException.class, () -> service.salvarImagem(mp));
        assertTrue(ex.getMessage().contains("Nome do arquivo"));
    }

    @Test
    void salvarImagem_inputStreamIo_throws() {
        MultipartFile bad = new MultipartFile() {
            public String getName() { return "f"; }
            public String getOriginalFilename() { return "img.png"; }
            public String getContentType() { return "image/png"; }
            public boolean isEmpty() { return false; }
            public long getSize() { return 0; }
            public byte[] getBytes() throws IOException { throw new IOException("fail"); }
            public java.io.InputStream getInputStream() throws IOException { throw new IOException("fail"); }
            public void transferTo(java.io.File dest) throws IOException, IllegalStateException { throw new IOException("fail"); }
        };

        FileUploadException ex = assertThrows(FileUploadException.class, () -> service.salvarImagem(bad));
        assertTrue(ex.getMessage().contains("Não foi possível salvar a imagem") || ex.getCause() != null);
    }

    @Test
    void converterESalvarWebpViaCli_whenCwebpMissing_throws() throws Exception {
        MockMultipartFile mp = new MockMultipartFile("file", "a.jpg", "image/jpeg", new byte[] {1,2,3});

        Path out = tempDir.resolve("out.webp");

        Method method = FileUploadService.class.getDeclaredMethod("converterESalvarWebpViaCli", MultipartFile.class, Path.class, Throwable.class);
        method.setAccessible(true);

        Exception thrown = assertThrows(Exception.class, () -> method.invoke(service, mp, out, new RuntimeException("cause")));
        // unwrap InvocationTargetException to check cause
        Throwable cause = thrown.getCause() != null ? thrown.getCause() : thrown;
        if (cause instanceof java.lang.reflect.InvocationTargetException) {
            cause = ((java.lang.reflect.InvocationTargetException) cause).getTargetException();
        }
        assertTrue(cause instanceof FileUploadException || cause.getCause() instanceof FileUploadException);
        // The assertion above ensures an exception; check that output file does not exist
        assertFalse(Files.exists(out));
    }

    @Test
    void obterWriterWebp_and_listWriters_run() throws Exception {
        Method listar = FileUploadService.class.getDeclaredMethod("listarWritersDisponiveis");
        listar.setAccessible(true);
        String writers = (String) listar.invoke(service);
        assertNotNull(writers);

        Method obter = FileUploadService.class.getDeclaredMethod("obterWriterWebp");
        obter.setAccessible(true);
        java.util.Optional<?> opt = (Optional<?>) obter.invoke(service);
        assertNotNull(opt);
    }

    @Test
    void aguardarProcesso_waitsAndReturns() throws Exception {
        // on mac/linux, use 'true' command to exit 0
        Process p = new ProcessBuilder("true").start();
        Method aguardar = FileUploadService.class.getDeclaredMethod("aguardarProcesso", Process.class);
        aguardar.setAccessible(true);
        Object rc = aguardar.invoke(service, p);
        assertEquals(0, rc);
    }
}
